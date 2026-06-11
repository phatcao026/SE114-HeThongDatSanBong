package com.example.backend.service.impl;

import com.example.backend.dto.request.BookingCreateRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.TimeSlot;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.TimeSlotRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.BookingLockService;
import com.example.backend.service.BookingService;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private static final BigDecimal DEPOSIT_RATE = BigDecimal.valueOf(0.3);

    private static final List<Enums.BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            Enums.BookingStatus.PENDING,
            Enums.BookingStatus.DEPOSIT_PAID,
            Enums.BookingStatus.CONFIRMED,
            Enums.BookingStatus.COMPLETED
    );

    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BookingLockService bookingLockService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              FieldRepository fieldRepository,
                              TimeSlotRepository timeSlotRepository,
                              UserRepository userRepository,
                              NotificationService notificationService,
                              BookingLockService bookingLockService) {
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.bookingLockService = bookingLockService;
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        return toResponses(bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUserId), null);
    }

    @Override
    public List<BookingResponse> getOwnerBookings() {
        ensureOwnerOrAdminRole();

        List<Field> fields = TokenUtils.hasRole("ADMIN")
                ? fieldRepository.findAll()
                : fieldRepository.findByOwnerId(TokenUtils.getCurrentUserId());

        List<Long> fieldIds = fields.stream().map(Field::getId).toList();
        if (fieldIds.isEmpty()) {
            return List.of();
        }

        return toResponses(bookingRepository.findByFieldIdInOrderByBookingDateDescCreatedAtDesc(fieldIds), null);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = findBooking(id);
        ensureCanViewBooking(booking);
        return toResponse(booking, null);
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new AppException(404, "Time slot not found"));
        Field field = findField(timeSlot.getFieldId());

        validateBookingRequest(request, field, timeSlot);

        String lockToken = bookingLockService.acquire(timeSlot.getId(), request.getBookingDate());

        try {
            if (hasActiveBooking(field.getId(), timeSlot.getId(), request.getBookingDate())) {
                throw new AppException(409, "Time slot has already been booked for this date");
            }

            Booking booking = new Booking();
            booking.setUserId(currentUserId);
            booking.setFieldId(field.getId());
            booking.setTimeSlotId(timeSlot.getId());
            booking.setBookingDate(request.getBookingDate());
            booking.setStatus(Enums.BookingStatus.PENDING);
            booking.setTotalAmount(timeSlot.getPrice());
            booking.setDepositAmount(calculateDeposit(timeSlot.getPrice()));
            booking.setNote(cleanOptional(request.getNote()));
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(booking);
            notifyBookingUpdate(savedBooking, "Booking created",
                    "Your booking #" + savedBooking.getId() + " was created. Please complete payment to confirm.");
            return toResponse(savedBooking, "Booking created. Please complete payment to confirm.");
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(409, "Time slot has already been booked for this date");
        } finally {
            bookingLockService.release(timeSlot.getId(), request.getBookingDate(), lockToken);
        }
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = findBooking(id);
        ensureCanCancelBooking(booking);

        if (booking.getStatus() == Enums.BookingStatus.CANCELLED) {
            throw new AppException(400, "Booking is already cancelled");
        }
        if (booking.getStatus() == Enums.BookingStatus.COMPLETED) {
            throw new AppException(400, "Completed booking cannot be cancelled");
        }

        booking.setStatus(Enums.BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        notifyBookingUpdate(savedBooking, "Booking cancelled",
                "Your booking #" + savedBooking.getId() + " was cancelled.");
        return toResponse(savedBooking, "Booking cancelled successfully");
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = findBooking(id);
        ensureCanManageBooking(booking);

        if (booking.getStatus() != Enums.BookingStatus.PENDING
                && booking.getStatus() != Enums.BookingStatus.DEPOSIT_PAID) {
            throw new AppException(400, "Only pending or deposit-paid bookings can be confirmed");
        }

        booking.setStatus(Enums.BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        notifyBookingUpdate(savedBooking, "Booking confirmed",
                "Your booking #" + savedBooking.getId() + " was confirmed.");
        return toResponse(savedBooking, "Booking confirmed successfully");
    }

    @Override
    @Transactional
    public BookingResponse completeBooking(Long id) {
        Booking booking = findBooking(id);
        ensureCanManageBooking(booking);

        if (booking.getStatus() != Enums.BookingStatus.CONFIRMED
                && booking.getStatus() != Enums.BookingStatus.DEPOSIT_PAID) {
            throw new AppException(400, "Only confirmed bookings can be completed");
        }

        booking.setStatus(Enums.BookingStatus.COMPLETED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        notifyBookingUpdate(savedBooking, "Booking completed",
                "Your booking #" + savedBooking.getId() + " was completed.");
        return toResponse(savedBooking, "Booking completed successfully");
    }

    private void notifyBookingUpdate(Booking booking, String title, String content) {
        if (booking.getUserId() == null) {
            return;
        }

        notificationService.createNotification(
                booking.getUserId(),
                title,
                content,
                Enums.NotificationType.BOOKING_UPDATE
        );
    }

    private void validateBookingRequest(BookingCreateRequest request, Field field, TimeSlot timeSlot) {
        if (request.getFieldId() != null && !request.getFieldId().equals(field.getId())) {
            throw new AppException(400, "Time slot does not belong to the selected field");
        }
        if (field.getStatus() != Enums.FieldStatus.AVAILABLE) {
            throw new AppException(400, "Field is not available");
        }
        if (timeSlot.getStatus() != Enums.TimeSlotStatus.AVAILABLE) {
            throw new AppException(400, "Time slot is not available");
        }
        if (timeSlot.getPrice() == null || timeSlot.getPrice().signum() < 0) {
            throw new AppException(400, "Time slot price is invalid");
        }
    }

    private boolean hasActiveBooking(Long fieldId, Long timeSlotId, java.time.LocalDate bookingDate) {
        return bookingRepository.existsByFieldIdAndTimeSlotIdAndBookingDateAndStatusIn(
                fieldId,
                timeSlotId,
                bookingDate,
                ACTIVE_BOOKING_STATUSES
        );
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Booking not found"));
    }

    private Field findField(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Field not found"));
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Time slot not found"));
    }

    private void ensureCanViewBooking(Booking booking) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        if (booking.getUserId().equals(currentUserId) || TokenUtils.hasRole("ADMIN")) {
            return;
        }

        Field field = findField(booking.getFieldId());
        if (field.getOwnerId() != null && field.getOwnerId().equals(currentUserId)) {
            return;
        }

        throw new AppException(403, "Access denied");
    }

    private void ensureCanCancelBooking(Booking booking) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        if (booking.getUserId().equals(currentUserId) || TokenUtils.hasRole("ADMIN")) {
            return;
        }

        ensureCanManageBooking(booking);
    }

    private void ensureCanManageBooking(Booking booking) {
        if (TokenUtils.hasRole("ADMIN")) {
            return;
        }

        ensureOwnerOrAdminRole();
        Long currentUserId = TokenUtils.getCurrentUserId();
        Field field = findField(booking.getFieldId());
        if (field.getOwnerId() == null || !field.getOwnerId().equals(currentUserId)) {
            throw new AppException(403, "Access denied");
        }
    }

    private void ensureOwnerOrAdminRole() {
        if (!TokenUtils.hasRole("OWNER") && !TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Only owners or admins can manage bookings");
        }
    }

    private BigDecimal calculateDeposit(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }

        return totalAmount.multiply(DEPOSIT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private List<BookingResponse> toResponses(List<Booking> bookings, String message) {
        if (bookings.isEmpty()) {
            return List.of();
        }

        Map<Long, Field> fieldsById = fieldRepository.findAllById(
                        bookings.stream().map(Booking::getFieldId).collect(Collectors.toSet())
                )
                .stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));

        Map<Long, TimeSlot> timeSlotsById = timeSlotRepository.findAllById(
                        bookings.stream().map(Booking::getTimeSlotId).collect(Collectors.toSet())
                )
                .stream()
                .collect(Collectors.toMap(TimeSlot::getId, Function.identity()));

        return bookings.stream()
                .map(booking -> toResponse(booking, fieldsById.get(booking.getFieldId()),
                        timeSlotsById.get(booking.getTimeSlotId()), message))
                .toList();
    }

    private BookingResponse toResponse(Booking booking, String message) {
        Field field = findField(booking.getFieldId());
        TimeSlot timeSlot = findTimeSlot(booking.getTimeSlotId());
        return toResponse(booking, field, timeSlot, message);
    }

    private BookingResponse toResponse(Booking booking, Field field, TimeSlot timeSlot, String message) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setFieldId(booking.getFieldId());
        response.setFieldName(field != null ? field.getName() : null);
        response.setTimeSlotId(booking.getTimeSlotId());
        response.setUserId(booking.getUserId());
        response.setStatus(booking.getStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setDepositAmount(booking.getDepositAmount());
        response.setBookingDate(booking.getBookingDate());
        response.setStartTime(timeSlot != null ? timeSlot.getStartTime() : null);
        response.setEndTime(timeSlot != null ? timeSlot.getEndTime() : null);
        response.setNote(booking.getNote());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        response.setMessage(message);
        return response;
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
