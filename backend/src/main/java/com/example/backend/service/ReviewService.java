package com.example.backend.service;

import com.example.backend.dto.request.ReviewCreateRequest;
import com.example.backend.dto.request.ReviewStatusUpdateRequest;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.utils.Enums;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewCreateRequest request);

    List<ReviewResponse> getWrittenReviews();

    List<ReviewResponse> getReceivedReviews();

    List<ReviewResponse> getReviewsByMatchRequest(Long matchRequestId);

    List<ReviewResponse> getAdminReviews(Enums.ReviewStatus status);

    ReviewResponse updateReviewStatus(Long id, ReviewStatusUpdateRequest request);
}
