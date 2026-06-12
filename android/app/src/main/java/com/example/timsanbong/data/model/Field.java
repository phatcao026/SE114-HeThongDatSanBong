package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class Field {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("description")
    private String description;

    @SerializedName("type")
    private String type; // FIVE_A_SIDE | SEVEN_A_SIDE | ELEVEN_A_SIDE

    @SerializedName("status")
    private String status; // AVAILABLE | MAINTENANCE | BOOKED

    @SerializedName("coverImage")
    private String coverImage;

    @SerializedName("timeSlots")
    private java.util.List<TimeSlotResponse> timeSlots; // only on detail response

    public long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getImageUrl() { return coverImage; }
    public java.util.List<TimeSlotResponse> getTimeSlots() { return timeSlots; }

    public boolean isAvailable() { return status == null || "AVAILABLE".equals(status); }

    /** Matches FieldFilter values from FilterBottomSheetFragment. */
    public String getFieldType() {
        if ("SEVEN_A_SIDE".equals(type)) return "7 người";
        if ("ELEVEN_A_SIDE".equals(type)) return "11 người";
        return "5 người";
    }

    /** Display label, e.g. "Sân 5". */
    public String getTypeLabel() {
        if ("SEVEN_A_SIDE".equals(type)) return "Sân 7";
        if ("ELEVEN_A_SIDE".equals(type)) return "Sân 11";
        return "Sân 5";
    }

    /** Lowest price across time slots (detail response only); 0 when unknown. */
    public double getPricePerHour() {
        if (timeSlots == null || timeSlots.isEmpty()) return 0;
        double min = 0;
        for (TimeSlotResponse slot : timeSlots) {
            if (slot.getPrice() == null) continue;
            double price = slot.getPrice();
            if (min == 0 || price < min) min = price;
        }
        return min;
    }

    public Field() {}
}
