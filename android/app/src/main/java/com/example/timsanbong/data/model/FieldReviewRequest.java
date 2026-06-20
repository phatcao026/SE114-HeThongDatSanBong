package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class FieldReviewRequest {
    @SerializedName("bookingId")
    private long bookingId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("imageUrl")
    private String imageUrl;

    public FieldReviewRequest(long bookingId, int rating, String comment, String imageUrl) {
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.imageUrl = imageUrl;
    }

    public long getBookingId() {
        return bookingId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
