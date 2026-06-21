package com.example.backend.controller;

import com.example.backend.dto.request.FairplayDecisionRequest;
import com.example.backend.dto.response.OpponentReviewResponse;
import com.example.backend.service.FairplayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/fairplay")
public class AdminFairplayController {

    private final FairplayService fairplayService;

    public AdminFairplayController(FairplayService fairplayService) {
        this.fairplayService = fairplayService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OpponentReviewResponse>> getPendingReviews() {
        return ResponseEntity.ok(fairplayService.getPendingReviews());
    }

    @GetMapping("/processed")
    public ResponseEntity<List<OpponentReviewResponse>> getProcessedReviews() {
        return ResponseEntity.ok(fairplayService.getProcessedReviews());
    }

    @GetMapping("/all")
    public ResponseEntity<List<OpponentReviewResponse>> getAllReviews(
            @RequestParam(value = "status", required = false) String status) {
        if ("PROCESSED".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(fairplayService.getProcessedReviews());
        }
        return ResponseEntity.ok(fairplayService.getPendingReviews());
    }

    @PutMapping("/resolve/{id}")
    public ResponseEntity<Map<String, String>> resolveReview(
            @PathVariable("id") Long id,
            @Valid @RequestBody FairplayDecisionRequest request) {
        fairplayService.resolveReview(id, request);
        return ResponseEntity.ok(Map.of("message", "Xử lý Tòa án Fairplay thành công!"));
    }
}
