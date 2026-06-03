package com.example.backend.entity;

import com.example.backend.utils.Enums;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewee_id")
    private Long revieweeId;

    @Column(name = "match_request_id")
    private Long matchRequestId;

    @Column(name = "score_change")
    private Integer scoreChange;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ai_suggested_penalty")
    private Integer aiSuggestedPenalty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private Enums.ReviewStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
