package com.example.backend.dto.request;

import com.example.backend.utils.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationCreateRequest {
    @NotNull(message = "User is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private Enums.NotificationType type;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Enums.NotificationType getType() {
        return type;
    }

    public void setType(Enums.NotificationType type) {
        this.type = type;
    }
}
