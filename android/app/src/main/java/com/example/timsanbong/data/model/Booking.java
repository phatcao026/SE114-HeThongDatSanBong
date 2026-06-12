package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("bookingId")
    private long bookingId;

    @SerializedName("fieldId")
    private long fieldId;

    @SerializedName("fieldName")
    private String fieldName;

    @SerializedName("timeSlotId")
    private long timeSlotId;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("depositAmount")
    private double depositAmount;

    @SerializedName("status")
    private String status; // PENDING | DEPOSIT_PAID | CONFIRMED | CANCELLED | COMPLETED

    public Booking() {}

    public long getBookingId() { return bookingId; }
    public long getId() { return bookingId; }
    public long getFieldId() { return fieldId; }
    public String getFieldName() { return fieldName; }
    public long getTimeSlotId() { return timeSlotId; }
    public String getBookingDate() { return bookingDate; }
    public String getStartTime() { return startTime != null ? startTime : ""; }
    public String getEndTime() { return endTime != null ? endTime : ""; }
    public double getTotalAmount() { return totalAmount; }
    public double getDepositAmount() { return depositAmount; }
    public String getStatus() { return status; }
    public double getTotalPrice() { return totalAmount; }
}
