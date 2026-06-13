package com.example.timsanbong.data.model;

public class ChatMessage {

    public static final String SIDE_ME = "me";
    public static final String SIDE_THEM = "them";
    public static final String SIDE_SYSTEM = "system";

    @com.google.gson.annotations.SerializedName("id")
    private long id;

    @com.google.gson.annotations.SerializedName("conversationId")
    private long conversationId;

    @com.google.gson.annotations.SerializedName("content")
    private String content;

    @com.google.gson.annotations.SerializedName("senderId")
    private long senderId;

    @com.google.gson.annotations.SerializedName("createdAt")
    private String createdAt;

    public ChatMessage() {}

    // Mock constructor for backwards compatibility
    public ChatMessage(String side, String content, String time) {
        this.content = content;
        this.createdAt = time;
        if (SIDE_ME.equals(side)) {
            this.senderId = 1; // Assuming current user is 1
        } else if (SIDE_THEM.equals(side)) {
            this.senderId = 2; // Assuming other user is 2
        } else {
            this.senderId = 0; // System
        }
    }

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
    public String getCreatedAt() { return createdAt; }

    public String getSide(long currentUserId) {
        if (senderId == 0) return SIDE_SYSTEM;
        return senderId == currentUserId ? SIDE_ME : SIDE_THEM;
    }
    
    public String getSide() {
        if (senderId == 0) return SIDE_SYSTEM;
        if (senderId == 1) return SIDE_ME; // Mock assumption
        return SIDE_THEM;
    }
    
    // UI Mock backwards compatibility
    public String getText() { return content; }
    public String getTime() { return createdAt != null ? createdAt : "12:00"; }
}
