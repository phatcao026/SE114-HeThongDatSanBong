package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private String url;
    private String message;
    private Long id;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private Enums.PaymentMethod paymentMethod;
    private String stripePaymentIntentId;
    private Enums.PaymentStatus status;
    private LocalDateTime createdAt;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Enums.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Enums.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public Enums.PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
