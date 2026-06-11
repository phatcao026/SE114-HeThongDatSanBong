package com.example.backend.dto.request;

import com.example.backend.utils.Enums;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewStatusUpdateRequest {
    @NotNull(message = "Review status is required")
    private Enums.ReviewStatus status;

    @Min(value = -20, message = "Score change must be at least -20")
    @Max(value = 10, message = "Score change must be at most 10")
    private Integer scoreChange;

    public Enums.ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.ReviewStatus status) {
        this.status = status;
    }

    public Integer getScoreChange() {
        return scoreChange;
    }

    public void setScoreChange(Integer scoreChange) {
        this.scoreChange = scoreChange;
    }
}
