package com.example.backend.dto.response;

public class RecommendedMatchResponse {
    private Long matchId;
    private String opponentNote;
    private String aiExplanation;
    private MatchPostResponse matchPost;

    public RecommendedMatchResponse() {}

    public RecommendedMatchResponse(Long matchId, String opponentNote, String aiExplanation, MatchPostResponse matchPost) {
        this.matchId = matchId;
        this.opponentNote = opponentNote;
        this.aiExplanation = aiExplanation;
        this.matchPost = matchPost;
    }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getOpponentNote() { return opponentNote; }
    public void setOpponentNote(String opponentNote) { this.opponentNote = opponentNote; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public MatchPostResponse getMatchPost() { return matchPost; }
    public void setMatchPost(MatchPostResponse matchPost) { this.matchPost = matchPost; }
}
