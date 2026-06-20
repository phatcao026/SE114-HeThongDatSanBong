package com.example.timsanbong.data.chat;

/**
 * DTO sent to the backend when asking a question to the AI chatbot.
 */
public class ChatCreateRequest {
    private String message;
    private String sessionId;

    public ChatCreateRequest() {
    }

    public ChatCreateRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

