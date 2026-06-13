package com.example.backend.service;

import com.example.backend.dto.response.BookingResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.Payment;
import com.example.backend.entity.TimeSlot;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.TimeSlotRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.BookingServiceImpl;
import com.example.backend.utils.Enums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingOperationsTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BookingLockService bookingLockService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private void mockAuthentication(Long userId, String role) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenAnswer(invocation -> 
            java.util.Collections.singletonList((org.springframework.security.core.GrantedAuthority) () -> "ROLE_" + role)
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCheckInBooking_Success() {
        Long ownerId = 1L;
        mockAuthentication(ownerId, "OWNER");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(2L);
        booking.setFieldId(100L);
        booking.setTimeSlotId(5L);
        booking.setStatus(Enums.BookingStatus.DEPOSIT_PAID);

        Field field = new Field();
        field.setId(100L);
        field.setOwnerId(ownerId);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(5L);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(timeSlotRepository.findById(5L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.checkInBooking(10L);

        assertNotNull(response);
        assertEquals(Enums.BookingStatus.CONFIRMED, response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void testCheckInBooking_InvalidStatus() {
        Long ownerId = 1L;
        mockAuthentication(ownerId, "OWNER");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(2L);
        booking.setFieldId(100L);
        booking.setTimeSlotId(5L);
        booking.setStatus(Enums.BookingStatus.PENDING); // Invalid status

        Field field = new Field();
        field.setId(100L);
        field.setOwnerId(ownerId);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));

        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.checkInBooking(10L);
        });

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Trạng thái đơn không hợp lệ"));
    }

    @Test
    public void testCheckOutBooking_Success_WithCashPayment() {
        Long ownerId = 1L;
        mockAuthentication(ownerId, "OWNER");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(2L);
        booking.setFieldId(100L);
        booking.setTimeSlotId(5L);
        booking.setStatus(Enums.BookingStatus.CONFIRMED);
        booking.setTotalAmount(BigDecimal.valueOf(100000));
        booking.setDepositAmount(BigDecimal.valueOf(30000));

        Field field = new Field();
        field.setId(100L);
        field.setOwnerId(ownerId);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(5L);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(timeSlotRepository.findById(5L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.checkOutBooking(10L, Enums.PaymentMethod.CASH);

        assertNotNull(response);
        assertEquals(Enums.BookingStatus.COMPLETED, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void testCheckOutBooking_AlreadyCompleted() {
        Long ownerId = 1L;
        mockAuthentication(ownerId, "OWNER");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(2L);
        booking.setFieldId(100L);
        booking.setTimeSlotId(5L);
        booking.setStatus(Enums.BookingStatus.COMPLETED); // Already completed

        Field field = new Field();
        field.setId(100L);
        field.setOwnerId(ownerId);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));

        AppException exception = assertThrows(AppException.class, () -> {
            bookingService.checkOutBooking(10L, Enums.PaymentMethod.CASH);
        });

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("hoàn tất trước đó"));
    }

    @Test
    public void testMarkAsNoShow_Success() {
        Long ownerId = 1L;
        mockAuthentication(ownerId, "OWNER");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(2L);
        booking.setFieldId(100L);
        booking.setTimeSlotId(5L);
        booking.setStatus(Enums.BookingStatus.DEPOSIT_PAID);

        Field field = new Field();
        field.setId(100L);
        field.setOwnerId(ownerId);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(5L);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(timeSlotRepository.findById(5L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.markAsNoShow(10L);

        assertNotNull(response);
        assertEquals(Enums.BookingStatus.CANCELLED, response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }
}
