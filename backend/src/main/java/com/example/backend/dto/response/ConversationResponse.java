package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationResponse {
    private Long id;
    private Enums.ConversationType type;
    private List<Long> memberIds;
    private List<String> memberNames;
    private Long lastMessageId;
    private Long lastMessageSenderId;
    private String lastMessageContent;
    private LocalDateTime lastMessageCreatedAt;
    private LocalDateTime createdAt;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enums.ConversationType getType() {
        return type;
    }

    public void setType(Enums.ConversationType type) {
        this.type = type;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public Long getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(Long lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public LocalDateTime getLastMessageCreatedAt() {
        return lastMessageCreatedAt;
    }

    public void setLastMessageCreatedAt(LocalDateTime lastMessageCreatedAt) {
        this.lastMessageCreatedAt = lastMessageCreatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
