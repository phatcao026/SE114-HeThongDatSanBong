package com.example.backend.controller;

import com.example.backend.dto.request.MatchRequestStatusUpdateRequest;
import com.example.backend.dto.response.MatchRequestResponse;
import com.example.backend.service.MatchRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/match-requests")
public class MatchRequestController {
    private final MatchRequestService matchRequestService;

    public MatchRequestController(MatchRequestService matchRequestService) {
        this.matchRequestService = matchRequestService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<MatchRequestResponse>> getMyRequests() {
        return ResponseEntity.ok(matchRequestService.getMyRequests());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MatchRequestResponse> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody MatchRequestStatusUpdateRequest request) {
        return ResponseEntity.ok(matchRequestService.updateRequestStatus(id, request));
    }
}
