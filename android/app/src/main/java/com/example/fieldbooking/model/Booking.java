package com.example.fieldbooking.model;

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

    public long getId() { return id; }
    public Field getField() { return field; }
    public User getUser() { return user; }
    public String getBookingDate() { return bookingDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
}
