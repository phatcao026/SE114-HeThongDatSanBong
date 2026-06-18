package com.example.backend.dto.request;

import com.example.backend.utils.Enums;

public class FieldUpdateRequest {
    private String name;
    private Enums.FieldType type;
    private String description;
    private String coverImage;
    private Enums.FieldStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Enums.FieldType getType() {
        return type;
    }

    public void setType(Enums.FieldType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Enums.FieldStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.FieldStatus status) {
        this.status = status;
    }
}
