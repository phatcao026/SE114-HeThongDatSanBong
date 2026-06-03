package com.example.backend.dto.request;

import com.example.backend.utils.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FieldCreateRequest {
    @NotBlank(message = "Field name is required")
    private String name;

    @NotNull(message = "Field type is required")
    private Enums.FieldType type;

    private String address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
