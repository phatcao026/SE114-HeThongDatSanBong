package com.example.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TimeSlotAvailabilityResponse {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private boolean available;

    public TimeSlotAvailabilityResponse() {
    }

    public TimeSlotAvailabilityResponse(Long id, LocalTime startTime, LocalTime endTime,
                                        BigDecimal price, boolean available) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
