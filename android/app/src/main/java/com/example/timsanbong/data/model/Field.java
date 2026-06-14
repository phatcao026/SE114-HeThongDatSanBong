package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Field {
    @SerializedName("id")
    private long id;

    @SerializedName("ownerId")
    private long ownerId;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("description")
    private String description;

    @SerializedName(value = "type", alternate = {"fieldType"})
    private String type;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "coverImage", alternate = {"imageUrl"})
    private String coverImage;

    @SerializedName("available")
    private boolean available;

    @SerializedName("pricePerHour")
    private double pricePerHour;

    @SerializedName("timeSlots")
    private List<TimeSlotResponse> timeSlots;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("reviewCount")
    private Long reviewCount;

    public Field() {}

    public Field(long id, String name, String address, double pricePerHour, String imageUrl,
                 String description, String fieldType, boolean available) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.pricePerHour = pricePerHour;
        this.coverImage = imageUrl;
        this.description = description;
        this.type = fieldType;
        this.available = available;
    }

    public long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getImageUrl() { return coverImage; }
    public List<TimeSlotResponse> getTimeSlots() { return timeSlots; }
    public Double getAverageRating() { return averageRating; }
    public Long getReviewCount() { return reviewCount; }

    public boolean isAvailable() {
        return available || status == null || "AVAILABLE".equalsIgnoreCase(status);
    }

    public String getFieldType() {
        if (type == null || type.trim().isEmpty()) return "5 người";
        if ("SEVEN_A_SIDE".equalsIgnoreCase(type) || "Sân 7".equalsIgnoreCase(type)
                || "7 người".equalsIgnoreCase(type)) {
            return "7 người";
        }
        if ("ELEVEN_A_SIDE".equalsIgnoreCase(type) || "Sân 11".equalsIgnoreCase(type)
                || "11 người".equalsIgnoreCase(type)) {
            return "11 người";
        }
        return "5 người";
    }

    public String getTypeLabel() {
        String fieldType = getFieldType();
        if ("7 người".equals(fieldType)) return "Sân 7";
        if ("11 người".equals(fieldType)) return "Sân 11";
        return "Sân 5";
    }

    public double getPricePerHour() {
        if (pricePerHour > 0) return pricePerHour;
        if (timeSlots == null || timeSlots.isEmpty()) return 0;
        double min = 0;
        for (TimeSlotResponse slot : timeSlots) {
            if (slot.getPrice() == null) continue;
            double price = slot.getPrice();
            if (min == 0 || price < min) min = price;
        }
        return min;
    }
}
