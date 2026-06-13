package com.example.backend.dto.ai;

public class AiRecommendationResult {
    private Long matchId;
    private String aiReason;

    public AiRecommendationResult() {}

    public AiRecommendationResult(Long matchId, String aiReason) {
        this.matchId = matchId;
        this.aiReason = aiReason;
    }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getAiReason() { return aiReason; }
    public void setAiReason(String aiReason) { this.aiReason = aiReason; }
}
