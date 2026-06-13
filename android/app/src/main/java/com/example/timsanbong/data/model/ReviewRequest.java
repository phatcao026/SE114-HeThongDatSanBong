package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("targetUserId")
    private long targetUserId;

    @SerializedName("matchId")
    private long matchId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comments")
    private String comments;

    public ReviewRequest(long targetUserId, long matchId, int rating, String comments) {
        this.targetUserId = targetUserId;
        this.matchId = matchId;
        this.rating = rating;
        this.comments = comments;
    }
}
