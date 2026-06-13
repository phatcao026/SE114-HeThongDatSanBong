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

    @SerializedName("user")
    private User user;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName(value = "totalPrice", alternate = {"totalAmount"})
    private double totalPrice;

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
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public long getId() { return id; }
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
    public User getUser() { return user; }
    public String getBookingDate() { return bookingDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
}
