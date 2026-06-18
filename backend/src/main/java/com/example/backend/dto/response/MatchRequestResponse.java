package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MatchRequestResponse {
    private Long id;
    private Long postId;
    private Long requesterId;
    private String requesterName;
    private String message;
    private Enums.RequestStatus status;
    private Enums.PostStatus postStatus;
    private Long postOwnerId;
    private LocalDate postDate;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Enums.RequestStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.RequestStatus status) {
        this.status = status;
    }

    public Enums.PostStatus getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(Enums.PostStatus postStatus) {
        this.postStatus = postStatus;
    }

    public Long getPostOwnerId() {
        return postOwnerId;
    }

    public void setPostOwnerId(Long postOwnerId) {
        this.postOwnerId = postOwnerId;
    }


    public LocalDate getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDate postDate) {
        this.postDate = postDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
