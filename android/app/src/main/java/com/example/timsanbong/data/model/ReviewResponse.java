package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ReviewResponse implements Serializable {
    @SerializedName("id")
    private Long id;
    @SerializedName("reviewerId")
    private Long reviewerId;
    @SerializedName("reviewerName")
    private String reviewerName;
    @SerializedName("revieweeId")
    private Long revieweeId;
    @SerializedName("revieweeName")
    private String revieweeName;
    @SerializedName("revieweeTrustScore")
    private Integer revieweeTrustScore;
    @SerializedName("matchRequestId")
    private Long matchRequestId;
    @SerializedName("matchPostId")
    private Long matchPostId;
    @SerializedName("scoreChange")
    private Integer scoreChange;
    @SerializedName("reason")
    private String reason;
    @SerializedName("aiSuggestedPenalty")
    private Integer aiSuggestedPenalty;
    @SerializedName("status")
    private String status;
    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() { return id; }
    public String getReviewerName() { return reviewerName; }
    public String getRevieweeName() { return revieweeName; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}