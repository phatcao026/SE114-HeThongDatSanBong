package com.example.backend.service.impl;

import com.example.backend.dto.request.FieldCreateRequest;
import com.example.backend.dto.request.FieldUpdateRequest;
import com.example.backend.dto.request.TimeSlotCreateRequest;
import com.example.backend.dto.request.TimeSlotUpdateRequest;
import com.example.backend.dto.response.FieldDetailResponse;
import com.example.backend.dto.response.FieldResponse;
import com.example.backend.dto.response.TimeSlotAvailabilityResponse;
import com.example.backend.dto.response.TimeSlotResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.TimeSlot;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.TimeSlotRepository;
import com.example.backend.repository.FieldReviewRepository;
import com.example.backend.service.FieldService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class FieldServiceImpl implements FieldService {
    private final FieldRepository fieldRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final FieldReviewRepository fieldReviewRepository;

    public FieldServiceImpl(FieldRepository fieldRepository,
                            TimeSlotRepository timeSlotRepository,
                            BookingRepository bookingRepository,
                            FieldReviewRepository fieldReviewRepository) {
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRepository = bookingRepository;
        this.fieldReviewRepository = fieldReviewRepository;
    }

    @Override
    public List<FieldResponse> getFields(Enums.FieldType type, BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);

        return fieldRepository.findFieldsWithFilters(type, minPrice, maxPrice)
                .stream()
                .map(this::toFieldResponse)
                .toList();
    }

    @Override
    public List<FieldResponse> getOwnerFields() {
        ensureOwnerOrAdminRole();
        Long ownerId = TokenUtils.getCurrentUserId();
        return fieldRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::toFieldResponse)
                .toList();
    }

    @Override
    public FieldDetailResponse getFieldById(Long id) {
        Field field = findFieldWithTimeSlots(id);
        return toFieldDetailResponse(field);
    }

    @Override
    public List<TimeSlotAvailabilityResponse> getFieldAvailability(Long id, LocalDate date) {
        if (date == null) {
            throw new AppException(400, "Date is required");
        }

        Field field = findFieldWithTimeSlots(id);
        List<Booking> bookings = bookingRepository.findByFieldIdAndBookingDate(id, date);
        Set<Long> bookedSlotIds = bookings.stream()
                .filter(booking -> booking.getStatus() != Enums.BookingStatus.CANCELLED)
                .map(Booking::getTimeSlotId)
                .collect(java.util.stream.Collectors.toSet());

        return field.getTimeSlots()
                .stream()
                .map(timeSlot -> {
                    boolean available = field.getStatus() == Enums.FieldStatus.AVAILABLE
                            && timeSlot.getStatus() == Enums.TimeSlotStatus.AVAILABLE
                            && !bookedSlotIds.contains(timeSlot.getId());
                    return toAvailabilityResponse(timeSlot, available);
                })
                .toList();
    }

    @Override
    @Transactional
    public FieldResponse createField(FieldCreateRequest request) {
        ensureOwnerOrAdminRole();

        Field field = new Field();
        field.setOwnerId(TokenUtils.getCurrentUserId());
        field.setName(cleanRequired(request.getName(), "Field name is required"));
        field.setType(request.getType());
        field.setDescription(cleanOptional(request.getDescription()));
        field.setCoverImage(cleanOptional(request.getCoverImage()));
        field.setStatus(request.getStatus() != null ? request.getStatus() : Enums.FieldStatus.AVAILABLE);
        field.setCreatedAt(LocalDateTime.now());
        field.setUpdatedAt(LocalDateTime.now());

        return toFieldResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional
    public FieldResponse updateField(Long id, FieldUpdateRequest request) {
        Field field = findField(id);
        ensureCanManageField(field);

        if (request.getName() != null) {
            field.setName(cleanRequired(request.getName(), "Field name is required"));
        }
        if (request.getType() != null) {
            field.setType(request.getType());
        }
        if (request.getDescription() != null) {
            field.setDescription(cleanOptional(request.getDescription()));
        }
        if (request.getCoverImage() != null) {
            field.setCoverImage(cleanOptional(request.getCoverImage()));
        }
        if (request.getStatus() != null) {
            field.setStatus(request.getStatus());
        }
        field.setUpdatedAt(LocalDateTime.now());

        return toFieldResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional
    public FieldResponse deleteField(Long id) {
        Field field = findField(id);
        ensureCanManageField(field);

        if (bookingRepository.existsByFieldId(id)) {
            throw new AppException(409, "Cannot delete field with bookings");
        }

        timeSlotRepository.deleteByFieldId(id);
        fieldRepository.delete(field);
        return toFieldResponse(field);
    }

    @Override
    @Transactional
    public TimeSlotResponse createTimeSlot(Long fieldId, TimeSlotCreateRequest request) {
        Field field = findField(fieldId);
        ensureCanManageField(field);

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validatePrice(request.getPrice());
        ensureNoOverlappingSlot(fieldId, null, request.getStartTime(), request.getEndTime());

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setFieldId(fieldId);
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setPrice(request.getPrice());
        timeSlot.setStatus(request.getStatus() != null ? request.getStatus() : Enums.TimeSlotStatus.AVAILABLE);

        return toTimeSlotResponse(timeSlotRepository.save(timeSlot));
    }

    @Override
    @Transactional
    public TimeSlotResponse updateTimeSlot(Long fieldId, Long slotId, TimeSlotUpdateRequest request) {
        Field field = findField(fieldId);
        ensureCanManageField(field);

        TimeSlot timeSlot = findTimeSlot(fieldId, slotId);
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : timeSlot.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : timeSlot.getEndTime();

        validateTimeRange(startTime, endTime);
        ensureNoOverlappingSlot(fieldId, slotId, startTime, endTime);

        if (request.getStartTime() != null) {
            timeSlot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            timeSlot.setEndTime(request.getEndTime());
        }
        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
            timeSlot.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            timeSlot.setStatus(request.getStatus());
        }

        return toTimeSlotResponse(timeSlotRepository.save(timeSlot));
    }

    @Override
    @Transactional
    public TimeSlotResponse deleteTimeSlot(Long fieldId, Long slotId) {
        Field field = findField(fieldId);
        ensureCanManageField(field);

        TimeSlot timeSlot = findTimeSlot(fieldId, slotId);
        if (bookingRepository.existsByTimeSlotId(slotId)) {
            throw new AppException(409, "Cannot delete time slot with bookings");
        }

        timeSlotRepository.delete(timeSlot);
        return toTimeSlotResponse(timeSlot);
    }

    private Field findField(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Field not found"));
    }

    private Field findFieldWithTimeSlots(Long id) {
        return fieldRepository.findByIdWithTimeSlots(id)
                .orElseThrow(() -> new AppException(404, "Field not found"));
    }

    private TimeSlot findTimeSlot(Long fieldId, Long slotId) {
        return timeSlotRepository.findByFieldIdAndId(fieldId, slotId)
                .orElseThrow(() -> new AppException(404, "Time slot not found"));
    }

    private void ensureOwnerOrAdminRole() {
        if (!TokenUtils.hasRole("OWNER") && !TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Only owners or admins can manage fields");
        }
    }

    private void ensureCanManageField(Field field) {
        if (TokenUtils.hasRole("ADMIN")) {
            return;
        }

        ensureOwnerOrAdminRole();
        Long currentUserId = TokenUtils.getCurrentUserId();
        if (field.getOwnerId() == null || !field.getOwnerId().equals(currentUserId)) {
            throw new AppException(403, "Access denied");
        }
    }

    private void ensureNoOverlappingSlot(Long fieldId, Long ignoredSlotId, LocalTime startTime, LocalTime endTime) {
        boolean hasOverlap = timeSlotRepository.findByFieldIdOrderByStartTimeAsc(fieldId)
                .stream()
                .filter(existingSlot -> ignoredSlotId == null || !existingSlot.getId().equals(ignoredSlotId))
                .anyMatch(existingSlot -> startTime.isBefore(existingSlot.getEndTime())
                        && endTime.isAfter(existingSlot.getStartTime()));

        if (hasOverlap) {
            throw new AppException(409, "Time slot overlaps with an existing slot");
        }
    }

    private FieldResponse toFieldResponse(Field field) {
        FieldResponse response = new FieldResponse();
        response.setId(field.getId());
        response.setName(field.getName());
        response.setDescription(field.getDescription());
        response.setType(field.getType());
        response.setStatus(field.getStatus());
        response.setCoverImage(field.getCoverImage());
        response.setCreatedAt(field.getCreatedAt());
        response.setUpdatedAt(field.getUpdatedAt());
        response.setAverageRating(fieldReviewRepository.getAverageRatingForField(field.getId()));
        response.setReviewCount(fieldReviewRepository.getReviewCountForField(field.getId()));
        // Include timeslots so clients (e.g. Owner dashboard) can show slot utilisation
        if (field.getTimeSlots() != null) {
            response.setTimeSlots(field.getTimeSlots()
                    .stream()
                    .map(this::toTimeSlotResponse)
                    .toList());
        }
        return response;
    }

    private FieldDetailResponse toFieldDetailResponse(Field field) {
        FieldDetailResponse response = new FieldDetailResponse();
        // reuse common mapping
        response.setId(field.getId());
        response.setName(field.getName());
        response.setDescription(field.getDescription());
        response.setType(field.getType());
        response.setStatus(field.getStatus());
        response.setCoverImage(field.getCoverImage());
        response.setCreatedAt(field.getCreatedAt());
        response.setUpdatedAt(field.getUpdatedAt());
        response.setAverageRating(fieldReviewRepository.getAverageRatingForField(field.getId()));
        response.setReviewCount(fieldReviewRepository.getReviewCountForField(field.getId()));
        if (field.getTimeSlots() != null) {
            response.setTimeSlots(field.getTimeSlots()
                    .stream()
                    .map(this::toTimeSlotResponse)
                    .toList());
        }
        return response;
    }

    private TimeSlotResponse toTimeSlotResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getFieldId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getPrice(),
                timeSlot.getStatus()
        );
    }

    private TimeSlotAvailabilityResponse toAvailabilityResponse(TimeSlot timeSlot, boolean available) {
        return new TimeSlotAvailabilityResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getPrice(),
                available
        );
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null) {
            validatePrice(minPrice);
        }
        if (maxPrice != null) {
            validatePrice(maxPrice);
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new AppException(400, "Min price cannot be greater than max price");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new AppException(400, "Price must be greater than or equal to 0");
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new AppException(400, "Start time and end time are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new AppException(400, "Start time must be before end time");
        }
    }

    private String cleanRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(400, message);
        }
        return value.trim();
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
