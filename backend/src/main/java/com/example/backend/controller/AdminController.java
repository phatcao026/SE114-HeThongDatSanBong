package com.example.backend.controller;

import com.example.backend.dto.response.AdminDashboardOverviewResponse;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.FieldReviewResponse;
import com.example.backend.dto.response.FieldResponse;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.AdminDashboardService;
import com.example.backend.service.FieldReviewService;
import com.example.backend.utils.Enums;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminDashboardService adminDashboardService;
    private final FieldReviewService fieldReviewService;

    public AdminController(AdminDashboardService adminDashboardService, FieldReviewService fieldReviewService) {
        this.adminDashboardService = adminDashboardService;
        this.fieldReviewService = fieldReviewService;
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<AdminDashboardOverviewResponse> getOverview() {
        return ResponseEntity.ok(adminDashboardService.getOverview());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) Enums.UserRole role,
            @RequestParam(required = false) Integer minTrustScore) {
        return ResponseEntity.ok(adminDashboardService.getUsers(role, minTrustScore));
    }

    @GetMapping("/fields")
    public ResponseEntity<List<FieldResponse>> getFields(
            @RequestParam(required = false) Enums.FieldStatus status) {
        return ResponseEntity.ok(adminDashboardService.getFields(status));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam(required = false) Enums.BookingStatus status) {
        return ResponseEntity.ok(adminDashboardService.getBookings(status));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) Enums.PaymentStatus status) {
        return ResponseEntity.ok(adminDashboardService.getPayments(status));
    }

    @GetMapping("/match-posts")
    public ResponseEntity<List<MatchPostResponse>> getMatchPosts(
            @RequestParam(required = false) Enums.PostStatus status) {
        return ResponseEntity.ok(adminDashboardService.getMatchPosts(status));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @RequestParam(required = false) Enums.ReviewStatus status) {
        return ResponseEntity.ok(adminDashboardService.getReviews(status));
    }

    @GetMapping("/field-reviews")
    public ResponseEntity<List<FieldReviewResponse>> getAdminFieldReviews() {
        return ResponseEntity.ok(fieldReviewService.getAllFieldReviews());
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable Long id) {
        adminDashboardService.lockUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        adminDashboardService.unlockUser(id);
        return ResponseEntity.ok().build();
    }
}
