package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class MessageRequest {
    @SerializedName("conversationId")
    private long conversationId;

    @SerializedName("content")
    private String content;

    public MessageRequest() {}

    public MessageRequest(long conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }

    public long getConversationId() { return conversationId; }

    public void setConversationId(long conversationId) { this.conversationId = conversationId; }
    public void setContent(String content) { this.content = content; }
}
