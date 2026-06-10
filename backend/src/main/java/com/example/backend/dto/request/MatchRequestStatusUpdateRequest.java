package com.example.backend.dto.request;

import com.example.backend.utils.Enums;
import jakarta.validation.constraints.NotNull;

public class MatchRequestStatusUpdateRequest {
    @NotNull(message = "Request status is required")
    private Enums.RequestStatus status;

    public Enums.RequestStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.RequestStatus status) {
        this.status = status;
    }
}
