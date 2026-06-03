package com.example.timsanbong.ui.customer;

public class SuggestedFieldItem {
    private final long id;
    private final String name;
    private final String imageUrl;
    private final String typeLabel;
    private final float rating;
    private final String distance;
    private final boolean hot;

    public SuggestedFieldItem(long id, String name, String imageUrl, String typeLabel, float rating, String distance, boolean hot) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.typeLabel = typeLabel;
        this.rating = rating;
        this.distance = distance;
        this.hot = hot;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public float getRating() {
        return rating;
    }

    public String getDistance() {
        return distance;
    }

    public boolean isHot() {
        return hot;
    }
}

