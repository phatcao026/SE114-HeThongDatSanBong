package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OpponentReviewResponse implements Serializable {
    @SerializedName("id")
    private Long id;
    @SerializedName("matchId")
    private Long matchId;
    @SerializedName("reviewerId")
    private Long reviewerId;
    @SerializedName("reviewerName")
    private String reviewerName;
    @SerializedName("revieweeId")
    private Long revieweeId;
    @SerializedName("revieweeName")
    private String revieweeName;
    @SerializedName("ratingType")
    private String ratingType;
    @SerializedName("comment")
    private String comment;
    @SerializedName("status")
    private String status;
    @SerializedName("pointsApplied")
    private Integer pointsApplied;
    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() { return id; }
    public Long getMatchId() { return matchId; }
    public String getReviewerName() { return reviewerName; }
    public String getRevieweeName() { return revieweeName; }
    public String getComment() { return comment; }
    public String getRatingType() { return ratingType; }
    public String getStatus() { return status; }
    public Integer getPointsApplied() { return pointsApplied; }
    public String getCreatedAt() { return createdAt; }
}