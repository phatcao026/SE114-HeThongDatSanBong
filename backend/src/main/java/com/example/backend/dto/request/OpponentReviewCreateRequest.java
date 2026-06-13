package com.example.backend.dto.request;

import com.example.backend.utils.Enums;
import jakarta.validation.constraints.NotNull;

public class OpponentReviewCreateRequest {

    @NotNull(message = "Match ID is required")
    private Long matchId;

    @NotNull(message = "Reviewee ID is required")
    private Long revieweeId;

    @NotNull(message = "Rating type is required")
    private Enums.OpponentRatingType ratingType;

    private String comment;

    private String imageUrl;

    public OpponentReviewCreateRequest() {}

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public Enums.OpponentRatingType getRatingType() { return ratingType; }
    public void setRatingType(Enums.OpponentRatingType ratingType) { this.ratingType = ratingType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
