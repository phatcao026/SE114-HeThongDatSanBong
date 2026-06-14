package com.example.timsanbong.data.model;

import java.math.BigDecimal;

public class PaymentResponse {
    private String url;
    private String message;
    private Long id;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String createdAt;

    public PaymentResponse() {}

    public PaymentResponse(Long id, Long userId, double amount, String paymentMethod, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = java.math.BigDecimal.valueOf(amount);
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
