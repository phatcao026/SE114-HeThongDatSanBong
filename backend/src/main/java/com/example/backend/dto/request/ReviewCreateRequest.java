package com.example.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewCreateRequest {
    @NotNull(message = "Match request is required")
    private Long matchRequestId;

    @NotNull(message = "Score change is required")
    @Min(value = -20, message = "Score change must be at least -20")
    @Max(value = 10, message = "Score change must be at most 10")
    private Integer scoreChange;

    @NotBlank(message = "Reason is required")
    private String reason;

    public Long getMatchRequestId() {
        return matchRequestId;
    }

    public void setMatchRequestId(Long matchRequestId) {
        this.matchRequestId = matchRequestId;
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
}
