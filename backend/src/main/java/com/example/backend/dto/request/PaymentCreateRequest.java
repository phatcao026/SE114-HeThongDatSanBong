package com.example.backend.dto.request;

import com.example.backend.utils.Enums;

import java.math.BigDecimal;

public class PaymentCreateRequest {
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private Enums.PaymentMethod paymentMethod;
    private String stripePaymentIntentId;
    private Enums.PaymentStatus status;

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
}
