package com.example.backend.service;

import com.example.backend.dto.request.FairplayDecisionRequest;
import com.example.backend.dto.request.OpponentReviewCreateRequest;
import com.example.backend.dto.response.OpponentReviewResponse;

import java.util.List;

public interface FairplayService {
    void submitReview(Long reviewerId, OpponentReviewCreateRequest request);
    List<OpponentReviewResponse> getPendingReviews();
    void resolveReview(Long reviewId, FairplayDecisionRequest request);
    List<Long> getMySubmittedMatchIds(Long reviewerId);
}
