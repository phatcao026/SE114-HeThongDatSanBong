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

    @SerializedName("pricePerHour")
    private double pricePerHour;

    @SerializedName(value = "imageUrl", alternate = {"coverImage"})
    private String imageUrl;

    @SerializedName("description")
    private String description;

    @SerializedName(value = "fieldType", alternate = {"type"})
    private String fieldType;

    @SerializedName("available")
    private boolean available;

    @SerializedName("status")
    private String status;

    @SerializedName("timeSlots")
    private List<TimeSlot> timeSlots;

    public long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getPricePerHour() { return pricePerHour; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getFieldType() { return fieldType; }
    public boolean isAvailable() { return available || "AVAILABLE".equalsIgnoreCase(status); }
    public List<TimeSlot> getTimeSlots() { return timeSlots; }

    public Field() {}

    public Field(long id, String name, String address, double pricePerHour, String imageUrl, String description, String fieldType, boolean available) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.pricePerHour = pricePerHour;
        this.imageUrl = imageUrl;
        this.description = description;
        this.fieldType = fieldType;
        this.available = available;
    }
}
