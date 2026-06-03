package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TimeSlotResponse {
    private Long id;
    private Long fieldId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private Enums.TimeSlotStatus status;

    public TimeSlotResponse() {
    }

    public TimeSlotResponse(Long id, Long fieldId, LocalTime startTime, LocalTime endTime,
                            BigDecimal price, Enums.TimeSlotStatus status) {
        this.id = id;
        this.fieldId = fieldId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
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

    public Enums.TimeSlotStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.TimeSlotStatus status) {
        this.status = status;
    }
}
