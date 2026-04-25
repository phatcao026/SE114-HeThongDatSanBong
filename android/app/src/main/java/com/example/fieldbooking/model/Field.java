package com.example.fieldbooking.model;

import com.google.gson.annotations.SerializedName;

public class Field {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("pricePerHour")
    private double pricePerHour;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("fieldType")
    private String fieldType;

    @SerializedName("available")
    private boolean available;

    public long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getPricePerHour() { return pricePerHour; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getFieldType() { return fieldType; }
    public boolean isAvailable() { return available; }
}
