package com.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class TeamInvitationDecisionRequest {
    @NotNull(message = "Accept decision is required")
    private Boolean accept;

    public Boolean getAccept() {
        return accept;
    }

    public void setAccept(Boolean accept) {
        this.accept = accept;
    }
}
