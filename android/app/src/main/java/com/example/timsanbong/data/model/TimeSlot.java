package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class TimeSlot {
    @SerializedName("id")
    private long id;

    @SerializedName("fieldId")
    private long fieldId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("price")
    private double price;

    @SerializedName("status")
    private String status;

    public TimeSlot() {
    }

    public TimeSlot(long id, long fieldId, String startTime, String endTime, double price, String status) {
        this.id = id;
        this.fieldId = fieldId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getFieldId() {
        return fieldId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
}
