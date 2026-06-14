package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {

    public static final String SIDE_ME = "me";
    public static final String SIDE_THEM = "them";
    public static final String SIDE_SYSTEM = "system";

    @SerializedName("id")
    private long id;

    @SerializedName("conversationId")
    private long conversationId;

    @SerializedName("content")
    private String content;

    @SerializedName("senderId")
    private long senderId;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("createdAt")
    private String createdAt;

    public ChatMessage() {}

    public ChatMessage(long id, long conversationId, String content, long senderId, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.content = content;
        this.senderId = senderId;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getConversationId() { return conversationId; }
    public String getContent() { return content; }
    public long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getCreatedAt() { return createdAt; }

    public String getSide(long currentUserId) {
        if (senderId == 0) return SIDE_SYSTEM;
        return senderId == currentUserId ? SIDE_ME : SIDE_THEM;
    }

    public String getText() { return content; }
    public String getTime() { return createdAt != null ? createdAt : ""; }
}
