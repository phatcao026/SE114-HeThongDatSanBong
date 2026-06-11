package com.example.backend.controller;

import com.example.backend.dto.request.ReviewCreateRequest;
import com.example.backend.dto.request.ReviewStatusUpdateRequest;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.service.ReviewService;
import com.example.backend.utils.Enums;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @GetMapping("/written")
    public ResponseEntity<List<ReviewResponse>> getWrittenReviews() {
        return ResponseEntity.ok(reviewService.getWrittenReviews());
    }

    @GetMapping("/received")
    public ResponseEntity<List<ReviewResponse>> getReceivedReviews() {
        return ResponseEntity.ok(reviewService.getReceivedReviews());
    }

    @GetMapping("/match-requests/{matchRequestId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMatchRequest(@PathVariable Long matchRequestId) {
        return ResponseEntity.ok(reviewService.getReviewsByMatchRequest(matchRequestId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ReviewResponse>> getAdminReviews(
            @RequestParam(required = false) Enums.ReviewStatus status) {
        return ResponseEntity.ok(reviewService.getAdminReviews(status));
    }

    @PutMapping("/admin/{id}/status")
    public ResponseEntity<ReviewResponse> updateReviewStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReviewStatusUpdateRequest request) {
        return ResponseEntity.ok(reviewService.updateReviewStatus(id, request));
    }
}
