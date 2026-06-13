package com.example.backend.controller;

import com.example.backend.dto.request.FieldReviewCreateRequest;
import com.example.backend.dto.response.FieldReviewResponse;
import com.example.backend.service.FieldReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/field")
public class FieldReviewController {

    private final FieldReviewService fieldReviewService;

    public FieldReviewController(FieldReviewService fieldReviewService) {
        this.fieldReviewService = fieldReviewService;
    }

    @PostMapping
    public ResponseEntity<FieldReviewResponse> createReview(@Valid @RequestBody FieldReviewCreateRequest request) {
        FieldReviewResponse response = fieldReviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fieldId}")
    public ResponseEntity<List<FieldReviewResponse>> getReviewsForField(@PathVariable Long fieldId) {
        List<FieldReviewResponse> response = fieldReviewService.getReviewsForField(fieldId);
        return ResponseEntity.ok(response);
    }
}
