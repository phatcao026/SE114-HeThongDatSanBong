package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName(value = "id", alternate = {"bookingId"})
    private long id;

    @SerializedName("field")
    private Field field;

    @SerializedName("fieldId")
    private long fieldId;

    @SerializedName("fieldName")
    private String fieldName;

    @SerializedName("timeSlotId")
    private long timeSlotId;

    @SerializedName("user")
    private User user;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName(value = "totalAmount", alternate = {"totalPrice"})
    private double totalAmount;

    @SerializedName("depositAmount")
    private double depositAmount;

    @SerializedName("status")
    private String status;

    public Booking() {}

    public Booking(long id, Field field, User user, String bookingDate, String startTime,
                   String endTime, double totalPrice, String status) {
        this.id = id;
        this.field = field;
        this.user = user;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalPrice;
        this.status = status;
    }

    public long getBookingId() { return id; }
    public long getId() { return id; }
    public long getFieldId() { return fieldId; }
    public long getTimeSlotId() { return timeSlotId; }
    public User getUser() { return user; }
    public String getBookingDate() { return bookingDate; }
    public String getStartTime() { return startTime != null ? startTime : ""; }
    public String getEndTime() { return endTime != null ? endTime : ""; }
    public double getTotalAmount() { return totalAmount; }
    public double getDepositAmount() { return depositAmount; }
    public double getTotalPrice() { return totalAmount; }
    public String getStatus() { return status; }

    public Field getField() {
        if (field != null) {
            return field;
        }
        if (fieldId > 0 || fieldName != null) {
            return new Field(fieldId, fieldName == null ? "Sân bóng" : fieldName, "", 0,
                    null, "", "", true);
        }
        return null;
    }

    public String getFieldName() {
        if (fieldName != null) return fieldName;
        if (field != null) return field.getName();
        return "Sân bóng";
    }
}
