package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class TimeSlotResponse {
    @SerializedName("id")
    private long id;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("price")
    private Double price;

    // GET fields/{id}/availability returns "available"; detail time slots return "status"
    @SerializedName("available")
    private Boolean available;

    @SerializedName("status")
    private String status; // AVAILABLE | PENDING | BOOKED

    public long getId() { return id; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }

    public boolean isAvailable() {
        if (available != null) return available;
        return "AVAILABLE".equals(status);
    }
}
