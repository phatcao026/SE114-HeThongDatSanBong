package com.example.timsanbong.data.model;

public class TimeSlotUpdateRequest {
    private final String startTime;
    private final String endTime;
    private final Double price;
    private final String status;

    public TimeSlotUpdateRequest(String startTime, String endTime, Double price, String status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = normalizeStatus(status);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    private String normalizeStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.contains("BOOKED") || normalized.contains("DAT") || normalized.contains("ĐẶT")) {
            return "BOOKED";
        }
        if (normalized.contains("PENDING") || normalized.contains("KHOA") || normalized.contains("KHÓA")) {
            return "PENDING";
        }
        return "AVAILABLE";
    }
}
