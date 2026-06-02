package com.example.timsanbong.data.model;

public class PaymentRequest {
    private final long bookingId;
    private final String fieldName;
    private final double totalPrice;

    public PaymentRequest(long bookingId, String fieldName, double totalPrice) {
        this.bookingId = bookingId;
        this.fieldName = fieldName;
        this.totalPrice = totalPrice;
    }

    public long getBookingId() {
        return bookingId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}

