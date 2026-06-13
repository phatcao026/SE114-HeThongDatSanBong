package com.example.backend.dto.ai;

public class AiOpponentDto {
    private Long matchId;
    private String playStyleNote;
    private int trustScore;

    public AiOpponentDto() {}

    public AiOpponentDto(Long matchId, String playStyleNote, int trustScore) {
        this.matchId = matchId;
        this.playStyleNote = playStyleNote;
        this.trustScore = trustScore;
    }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getPlayStyleNote() { return playStyleNote; }
    public void setPlayStyleNote(String playStyleNote) { this.playStyleNote = playStyleNote; }

    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }

    @Override
    public String toString() {
        return "AiOpponentDto{" +
                "matchId=" + matchId +
                ", playStyleNote='" + playStyleNote + '\'' +
                ", trustScore=" + trustScore +
                '}';
    }
}
