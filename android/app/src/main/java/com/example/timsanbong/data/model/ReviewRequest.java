package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("revieweeId")
    private long revieweeId;

    @SerializedName("matchId")
    private long matchId;

    @SerializedName("ratingType")
    private String ratingType;

    @SerializedName("comment")
    private String comment;

    public ReviewRequest(long revieweeId, long matchId, String ratingType, String comment) {
        this.revieweeId = revieweeId;
        this.matchId = matchId;
        this.ratingType = ratingType;
        this.comment = comment;
    }

    // Helper for MatchHistoryActivity which uses stars
    public static ReviewRequest fromRating(long revieweeId, long matchId, int stars, String comment) {
        String type = (stars >= 4) ? "GOOD" : (stars <= 2 ? "BAD_BEHAVIOR" : "GOOD");
        return new ReviewRequest(revieweeId, matchId, type, comment);
    }

    public long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(long revieweeId) { this.revieweeId = revieweeId; }

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public String getRatingType() { return ratingType; }
    public void setRatingType(String ratingType) { this.ratingType = ratingType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
