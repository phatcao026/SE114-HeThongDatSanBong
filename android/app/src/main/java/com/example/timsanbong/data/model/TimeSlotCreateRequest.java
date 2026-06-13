package com.example.timsanbong.data.model;

public class TimeSlotCreateRequest {
    private final String startTime;
    private final String endTime;
    private final double price;
    private final String status;

    public TimeSlotCreateRequest(String startTime, String endTime, double price, String status) {
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

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    private String normalizeStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "AVAILABLE";
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
