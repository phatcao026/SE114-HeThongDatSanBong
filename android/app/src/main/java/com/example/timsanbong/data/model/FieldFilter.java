package com.example.timsanbong.data.model;

public class FieldFilter {
    public String fieldType = null;
    public int priceRange = 0;
    public boolean availableOnly = false;

    public boolean isActive() {
        return fieldType != null || priceRange != 0 || availableOnly;
    }
}
