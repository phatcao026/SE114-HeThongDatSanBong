package com.example.backend.dto.response;

import java.util.List;

public class FieldDetailResponse extends FieldResponse {
    private List<TimeSlotResponse> timeSlots;

    public List<TimeSlotResponse> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlotResponse> timeSlots) {
        this.timeSlots = timeSlots;
    }
}
