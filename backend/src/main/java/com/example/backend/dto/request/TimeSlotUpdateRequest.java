package com.example.backend.dto.request;

import com.example.backend.utils.Enums;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TimeSlotUpdateRequest {
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Enums.TimeSlotStatus status;

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

    public Enums.TimeSlotStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.TimeSlotStatus status) {
        this.status = status;
    }
}
