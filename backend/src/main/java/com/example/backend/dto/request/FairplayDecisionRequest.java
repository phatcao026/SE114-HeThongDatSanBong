package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class FairplayDecisionRequest {

    @JsonProperty("isAccepted")
    @NotNull(message = "isAccepted is required")
    private Boolean isAccepted;

    private Integer pointsApplied;

    public FairplayDecisionRequest() {}

    public Boolean getIsAccepted() { return isAccepted; }
    public void setIsAccepted(Boolean isAccepted) { this.isAccepted = isAccepted; }

    public Integer getPointsApplied() { return pointsApplied; }
    public void setPointsApplied(Integer pointsApplied) { this.pointsApplied = pointsApplied; }
}
