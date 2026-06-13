package com.example.backend.entity;

import com.example.backend.utils.Enums;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "captain_id")
    private Long captainId;

    @Enumerated(EnumType.STRING)
    private Enums.TeamLevel level;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "conversation_id")
    private Long conversationId;

    @ManyToOne
    @JoinColumn(name = "captain_id", insertable = false, updatable = false)
    private User captain;

    @OneToOne
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

    // getters & setters
    public Team() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCaptainId() { return captainId; }
    public void setCaptainId(Long captainId) { this.captainId = captainId; }

    public Enums.TeamLevel getLevel() { return level; }
    public void setLevel(Enums.TeamLevel level) { this.level = level; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public User getCaptain() { return captain; }
    public void setCaptain(User captain) { this.captain = captain; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
}
