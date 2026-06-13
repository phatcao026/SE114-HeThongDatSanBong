package com.example.backend.entity.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RedisHash(value = "ChatSession", timeToLive = 3600)
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String sessionId;

    private List<MessageNode> messages = new ArrayList<>();

    public ChatSession() {}

    public ChatSession(String sessionId, List<MessageNode> messages) {
        this.sessionId = sessionId;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<MessageNode> getMessages() { return messages; }
    public void setMessages(List<MessageNode> messages) { this.messages = messages; }

    public static class MessageNode implements Serializable {
        private static final long serialVersionUID = 1L;

        private String role;
        private String content;

        public MessageNode() {}

        public MessageNode(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
