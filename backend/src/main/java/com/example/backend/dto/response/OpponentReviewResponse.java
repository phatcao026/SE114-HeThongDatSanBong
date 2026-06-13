package com.example.backend.dto.response;

import com.example.backend.utils.Enums;
import java.time.LocalDateTime;

public class OpponentReviewResponse {
    private Long id;
    private Long matchId;
    private Long reviewerId;
    private String reviewerName;
    private Long revieweeId;
    private String revieweeName;
    private Enums.OpponentRatingType ratingType;
    private String comment;
    private Enums.FairplayStatus status;
    private Integer pointsApplied;
    private String imageUrl;
    private LocalDateTime createdAt;

    public OpponentReviewResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public String getRevieweeName() { return revieweeName; }
    public void setRevieweeName(String revieweeName) { this.revieweeName = revieweeName; }

    public Enums.OpponentRatingType getRatingType() { return ratingType; }
    public void setRatingType(Enums.OpponentRatingType ratingType) { this.ratingType = ratingType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Enums.FairplayStatus getStatus() { return status; }
    public void setStatus(Enums.FairplayStatus status) { this.status = status; }

    public Integer getPointsApplied() { return pointsApplied; }
    public void setPointsApplied(Integer pointsApplied) { this.pointsApplied = pointsApplied; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
