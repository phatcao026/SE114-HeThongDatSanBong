package com.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class ConversationCreateRequest {
    @NotNull(message = "Recipient is required")
    private Long recipientId;

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
}
