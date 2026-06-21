package com.example.backend.controller;

import com.example.backend.dto.response.OpponentReviewResponse;
import com.example.backend.service.FairplayService;
import com.example.backend.utils.TokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fairplay")
public class FairplayController {

    private final FairplayService fairplayService;

    public FairplayController(FairplayService fairplayService) {
        this.fairplayService = fairplayService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Map<String, String>> submitReview(@Valid @RequestBody com.example.backend.dto.request.OpponentReviewCreateRequest request) {
        Long reviewerId = TokenUtils.getCurrentUserId();
        fairplayService.submitReview(reviewerId, request);
        return ResponseEntity.ok(Map.of("message", "Đã gửi đánh giá lên Tòa án Fairplay chờ xử lý!"));
    }

    @GetMapping("/my-submitted")
    public ResponseEntity<List<Long>> getMySubmittedReviews() {
        Long reviewerId = TokenUtils.getCurrentUserId();
        return ResponseEntity.ok(fairplayService.getMySubmittedMatchIds(reviewerId));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<OpponentReviewResponse>> getMyReviews() {
        Long reviewerId = TokenUtils.getCurrentUserId();
        return ResponseEntity.ok(fairplayService.getMyReviews(reviewerId));
    }
}
