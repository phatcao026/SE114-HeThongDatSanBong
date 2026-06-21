package com.example.backend.service;

import com.example.backend.dto.request.FieldReviewCreateRequest;
import com.example.backend.dto.response.FieldReviewResponse;

import java.util.List;

public interface FieldReviewService {
    FieldReviewResponse createReview(FieldReviewCreateRequest request);
    List<FieldReviewResponse> getReviewsForField(Long fieldId);
    List<FieldReviewResponse> getAllFieldReviews();
}
