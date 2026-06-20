package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class FieldReviewResponse {
    @SerializedName("id")
    private long id;

    @SerializedName("bookingId")
    private long bookingId;

    @SerializedName("fieldId")
    private long fieldId;

    @SerializedName("reviewerId")
    private long reviewerId;

    @SerializedName("reviewerName")
    private String reviewerName;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() {
        return id;
    }

    public long getBookingId() {
        return bookingId;
    }

    public long getFieldId() {
        return fieldId;
    }

    public long getReviewerId() {
        return reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
