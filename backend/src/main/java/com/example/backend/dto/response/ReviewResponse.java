package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.time.LocalDateTime;

public class ReviewResponse {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private Long revieweeId;
    private String revieweeName;
    private Integer revieweeTrustScore;
    private Long matchRequestId;
    private Long matchPostId;
    private Integer scoreChange;
    private String reason;
    private Integer aiSuggestedPenalty;
    private Enums.ReviewStatus status;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public Integer getRevieweeTrustScore() {
        return revieweeTrustScore;
    }

    public void setRevieweeTrustScore(Integer revieweeTrustScore) {
        this.revieweeTrustScore = revieweeTrustScore;
    }

    public Long getMatchRequestId() {
        return matchRequestId;
    }

    public void setMatchRequestId(Long matchRequestId) {
        this.matchRequestId = matchRequestId;
    }

    public Long getMatchPostId() {
        return matchPostId;
    }

    public void setMatchPostId(Long matchPostId) {
        this.matchPostId = matchPostId;
    }

    public Integer getScoreChange() {
        return scoreChange;
    }

    public void setScoreChange(Integer scoreChange) {
        this.scoreChange = scoreChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getAiSuggestedPenalty() {
        return aiSuggestedPenalty;
    }

    public void setAiSuggestedPenalty(Integer aiSuggestedPenalty) {
        this.aiSuggestedPenalty = aiSuggestedPenalty;
    }

    public Enums.ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.ReviewStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
