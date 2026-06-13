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

    @PutMapping("/resolve/{id}")
    public ResponseEntity<Map<String, String>> resolveReview(
            @PathVariable("id") Long id,
            @Valid @RequestBody FairplayDecisionRequest request) {
        fairplayService.resolveReview(id, request);
        return ResponseEntity.ok(Map.of("message", "Xử lý Tòa án Fairplay thành công!"));
    }
}
