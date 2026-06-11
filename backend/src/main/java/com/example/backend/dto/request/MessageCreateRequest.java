package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MessageCreateRequest {
    @NotBlank(message = "Message content is required")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
