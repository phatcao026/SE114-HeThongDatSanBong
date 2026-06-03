package com.example.backend.entity;

import com.example.backend.utils.Enums;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_requests")
public class MatchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private Enums.RequestStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private MatchPost post;

    @ManyToOne
    @JoinColumn(name = "requester_id", insertable = false, updatable = false)
    private User requester;

    // getters/setters
    public MatchRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Enums.RequestStatus getStatus() { return status; }
    public void setStatus(Enums.RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
