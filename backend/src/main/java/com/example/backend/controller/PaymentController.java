package com.example.backend.controller;

import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getBookingPayments(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getBookingPayments(bookingId));
    }

    @PostMapping("/create-session/{bookingId}")
    public ResponseEntity<PaymentResponse> createPaymentSession(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.createCheckoutSession(bookingId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> stripeWebhook(@RequestBody(required = false) String payload,
                                                HttpServletRequest request) {
        String sigHeader = request.getHeader("Stripe-Signature");
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("Received");
    }
}
