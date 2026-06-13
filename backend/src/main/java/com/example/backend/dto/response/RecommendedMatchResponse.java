package com.example.backend.dto.response;

public class RecommendedMatchResponse {
    private Long matchId;
    private String opponentNote;
    private String aiExplanation;

    public RecommendedMatchResponse() {}

    public RecommendedMatchResponse(Long matchId, String opponentNote, String aiExplanation) {
        this.matchId = matchId;
        this.opponentNote = opponentNote;
        this.aiExplanation = aiExplanation;
    }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getOpponentNote() { return opponentNote; }
    public void setOpponentNote(String opponentNote) { this.opponentNote = opponentNote; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
}
