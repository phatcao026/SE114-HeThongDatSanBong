package com.example.backend.service;

import com.example.backend.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse createCheckoutSession(Long bookingId);

    List<PaymentResponse> getMyPayments();

    List<PaymentResponse> getBookingPayments(Long bookingId);

    void handleStripeWebhook(String payload, String sigHeader);
}
