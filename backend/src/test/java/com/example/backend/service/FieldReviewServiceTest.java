package com.example.backend.service;

import com.example.backend.dto.request.FieldReviewCreateRequest;
import com.example.backend.dto.response.FieldReviewResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.FieldReview;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.FieldReviewRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.FieldReviewServiceImpl;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FieldReviewServiceTest {

    @Mock
    private FieldReviewRepository fieldReviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FieldReviewServiceImpl fieldReviewService;

    private void mockAuthentication(Long userId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateReview_Success() {
        Long userId = 1L;
        mockAuthentication(userId);

        FieldReviewCreateRequest request = new FieldReviewCreateRequest();
        request.setBookingId(10L);
        request.setRating(5);
        request.setComment("Nice field");
        request.setImageUrl("http://image.com");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setFieldId(100L);
        booking.setUserId(userId);
        booking.setStatus(Enums.BookingStatus.COMPLETED);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldReviewRepository.existsByBookingId(10L)).thenReturn(false);

        FieldReview savedReview = new FieldReview();
        savedReview.setId(50L);
        savedReview.setBookingId(10L);
        savedReview.setFieldId(100L);
        savedReview.setReviewerId(userId);
        savedReview.setRating(5);
        savedReview.setComment("Nice field");
        savedReview.setImageUrl("http://image.com");
        savedReview.setCreatedAt(LocalDateTime.now());

        when(fieldReviewRepository.save(any(FieldReview.class))).thenReturn(savedReview);

        User reviewer = new User();
        reviewer.setId(userId);
        reviewer.setFullName("John Doe");
        when(userRepository.findById(userId)).thenReturn(Optional.of(reviewer));

        FieldReviewResponse response = fieldReviewService.createReview(request);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals(10L, response.getBookingId());
        assertEquals(100L, response.getFieldId());
        assertEquals(userId, response.getReviewerId());
        assertEquals("John Doe", response.getReviewerName());
        assertEquals(5, response.getRating());
        assertEquals("Nice field", response.getComment());
        assertEquals("http://image.com", response.getImageUrl());
    }

    @Test
    public void testCreateReview_Forbidden_NotOwned() {
        Long userId = 1L;
        mockAuthentication(userId);

        FieldReviewCreateRequest request = new FieldReviewCreateRequest();
        request.setBookingId(10L);
        request.setRating(5);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(2L); // owned by user 2
        booking.setStatus(Enums.BookingStatus.COMPLETED);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        AppException exception = assertThrows(AppException.class, () -> {
            fieldReviewService.createReview(request);
        });

        assertEquals(403, exception.getStatusCode());
        assertEquals("You do not have permission to review this booking", exception.getMessage());
    }

    @Test
    public void testCreateReview_BadRequest_NotCompleted() {
        Long userId = 1L;
        mockAuthentication(userId);

        FieldReviewCreateRequest request = new FieldReviewCreateRequest();
        request.setBookingId(10L);
        request.setRating(5);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(userId);
        booking.setStatus(Enums.BookingStatus.PENDING); // Not completed

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        AppException exception = assertThrows(AppException.class, () -> {
            fieldReviewService.createReview(request);
        });

        assertEquals(400, exception.getStatusCode());
        assertEquals("Only completed bookings can be reviewed", exception.getMessage());
    }

    @Test
    public void testCreateReview_Conflict_AlreadyReviewed() {
        Long userId = 1L;
        mockAuthentication(userId);

        FieldReviewCreateRequest request = new FieldReviewCreateRequest();
        request.setBookingId(10L);
        request.setRating(5);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(userId);
        booking.setStatus(Enums.BookingStatus.COMPLETED);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(fieldReviewRepository.existsByBookingId(10L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            fieldReviewService.createReview(request);
        });

        assertEquals(409, exception.getStatusCode());
        assertEquals("You have already reviewed this booking", exception.getMessage());
    }

    @Test
    public void testGetReviewsForField_FieldNotFound() {
        when(fieldRepository.existsById(100L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            fieldReviewService.getReviewsForField(100L);
        });

        assertEquals(404, exception.getStatusCode());
        assertEquals("Field not found", exception.getMessage());
    }

    @Test
    public void testGetReviewsForField_Success() {
        when(fieldRepository.existsById(100L)).thenReturn(true);

        FieldReview review1 = new FieldReview();
        review1.setId(1L);
        review1.setBookingId(10L);
        review1.setFieldId(100L);
        review1.setReviewerId(1L);
        review1.setRating(5);
        review1.setCreatedAt(LocalDateTime.now());

        FieldReview review2 = new FieldReview();
        review2.setId(2L);
        review2.setBookingId(11L);
        review2.setFieldId(100L);
        review2.setReviewerId(2L);
        review2.setRating(4);
        review2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(fieldReviewRepository.findByFieldIdOrderByCreatedAtDesc(100L))
                .thenReturn(List.of(review1, review2));

        User user1 = new User();
        user1.setId(1L);
        user1.setFullName("User One");

        User user2 = new User();
        user2.setId(2L);
        user2.setFullName("User Two");

        when(userRepository.findAllById(any())).thenReturn(List.of(user1, user2));

        List<FieldReviewResponse> results = fieldReviewService.getReviewsForField(100L);

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals("User One", results.get(0).getReviewerName());
        assertEquals(2L, results.get(1).getId());
        assertEquals("User Two", results.get(1).getReviewerName());
    }
}
