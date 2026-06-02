package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("id")
    private long id;

    @SerializedName("field")
    private Field field;

    @SerializedName("user")
    private User user;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("totalPrice")
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
    public Field getField() { return field; }
    public User getUser() { return user; }
    public String getBookingDate() { return bookingDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
}
