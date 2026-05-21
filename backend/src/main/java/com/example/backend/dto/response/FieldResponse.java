package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.time.LocalDateTime;

public class FieldResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String address;
    private String description;
    private Enums.FieldType type;
    private Enums.FieldStatus status;
    private String coverImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Enums.FieldType getType() {
        return type;
    }

    public void setType(Enums.FieldType type) {
        this.type = type;
    }

    public Enums.FieldStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.FieldStatus status) {
        this.status = status;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
