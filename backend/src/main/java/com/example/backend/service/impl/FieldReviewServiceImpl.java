package com.example.backend.service.impl;

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
import com.example.backend.service.FieldReviewService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FieldReviewServiceImpl implements FieldReviewService {

    private final FieldReviewRepository fieldReviewRepository;
    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;

    public FieldReviewServiceImpl(FieldReviewRepository fieldReviewRepository,
                                  BookingRepository bookingRepository,
                                  FieldRepository fieldRepository,
                                  UserRepository userRepository) {
        this.fieldReviewRepository = fieldReviewRepository;
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FieldReviewResponse createReview(FieldReviewCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(404, "Booking not found"));

        if (!booking.getUserId().equals(currentUserId)) {
            throw new AppException(403, "You do not have permission to review this booking");
        }

        if (booking.getStatus() != Enums.BookingStatus.COMPLETED) {
            throw new AppException(400, "Only completed bookings can be reviewed");
        }

        if (fieldReviewRepository.existsByBookingId(request.getBookingId())) {
            throw new AppException(409, "You have already reviewed this booking");
        }

        FieldReview review = new FieldReview();
        review.setBookingId(booking.getId());
        review.setFieldId(booking.getFieldId());
        review.setReviewerId(currentUserId);
        review.setRating(request.getRating());
        review.setComment(request.getComment() != null ? request.getComment().trim() : null);
        review.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        review.setCreatedAt(LocalDateTime.now());

        FieldReview savedReview = fieldReviewRepository.save(review);
        
        User reviewer = userRepository.findById(currentUserId).orElse(null);
        return toReviewResponse(savedReview, reviewer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldReviewResponse> getReviewsForField(Long fieldId) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new AppException(404, "Field not found");
        }

        List<FieldReview> reviews = fieldReviewRepository.findByFieldIdOrderByCreatedAtDesc(fieldId);
        if (reviews.isEmpty()) {
            return List.of();
        }

        Set<Long> reviewerIds = reviews.stream()
                .map(FieldReview::getReviewerId)
                .collect(Collectors.toSet());

        Map<Long, User> usersById = userRepository.findAllById(reviewerIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return reviews.stream()
                .map(review -> toReviewResponse(review, usersById.get(review.getReviewerId())))
                .toList();
    }

    private FieldReviewResponse toReviewResponse(FieldReview review, User reviewer) {
        FieldReviewResponse response = new FieldReviewResponse();
        response.setId(review.getId());
        response.setBookingId(review.getBookingId());
        response.setFieldId(review.getFieldId());
        response.setReviewerId(review.getReviewerId());
        response.setReviewerName(reviewer != null ? reviewer.getFullName() : null);
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setImageUrl(review.getImageUrl());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
