package com.example.backend.service;

import com.example.backend.dto.response.AdminDashboardOverviewResponse;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.FieldResponse;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.utils.Enums;

import java.util.List;

public interface AdminDashboardService {
    AdminDashboardOverviewResponse getOverview();

    List<UserResponse> getUsers(Enums.UserRole role, Integer minTrustScore);

    List<FieldResponse> getFields(Enums.FieldStatus status);

    List<BookingResponse> getBookings(Enums.BookingStatus status);

    List<PaymentResponse> getPayments(Enums.PaymentStatus status);

    List<MatchPostResponse> getMatchPosts(Enums.PostStatus status);

    List<ReviewResponse> getReviews(Enums.ReviewStatus status);

    void lockUser(Long id);

    void unlockUser(Long id);
}
