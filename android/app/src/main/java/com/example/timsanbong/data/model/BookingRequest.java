package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {
    @SerializedName("fieldId")
    private long fieldId;

    @SerializedName("timeSlotId")
    private long timeSlotId;

    @SerializedName("bookingDate")
    private String bookingDate;

    public BookingRequest() {}

    public void setFieldId(long fieldId) { this.fieldId = fieldId; }
    public void setTimeSlotId(long timeSlotId) { this.timeSlotId = timeSlotId; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
}
