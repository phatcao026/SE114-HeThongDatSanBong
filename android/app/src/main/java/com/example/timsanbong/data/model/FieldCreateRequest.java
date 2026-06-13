package com.example.timsanbong.data.model;

public class FieldCreateRequest {
    private final String name;
    private final String type;
    private final String address;
    private final String description;
    private final String coverImage;
    private final String status;

    public FieldCreateRequest(String name, String type, String address, String description,
                              String coverImage, String status) {
        this.name = name;
        this.type = normalizeType(type);
        this.address = address;
        this.description = description;
        this.coverImage = coverImage;
        this.status = normalizeStatus(status);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public String getStatus() {
        return status;
    }

    private String normalizeType(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.contains("11") || normalized.contains("ELEVEN")) {
            return "ELEVEN_A_SIDE";
        }
        if (normalized.contains("7") || normalized.contains("SEVEN")) {
            return "SEVEN_A_SIDE";
        }
        if (normalized.contains("5") || normalized.contains("FIVE")) {
            return "FIVE_A_SIDE";
        }
        return normalized;
    }

    private String normalizeStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "AVAILABLE";
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.contains("MAINTENANCE") || normalized.contains("TAM") || normalized.contains("BẢO")) {
            return "MAINTENANCE";
        }
        if (normalized.contains("BOOKED") || normalized.contains("DONG") || normalized.contains("ĐÓNG")) {
            return "BOOKED";
        }
        return "AVAILABLE";
    }
}
