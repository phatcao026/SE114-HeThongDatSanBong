package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RecommendedMatch implements Serializable {
    @SerializedName("matchId")
    private Long matchId;

    @SerializedName("opponentNote")
    private String opponentNote;

    @SerializedName("aiExplanation")
    private String aiExplanation;

    @SerializedName("matchPost")
    private MatchPost matchPost;

    public Long getMatchId() { return matchId; }
    public String getOpponentNote() { return opponentNote; }
    public String getAiExplanation() { return aiExplanation; }
    public MatchPost getMatchPost() { return matchPost; }
    public void setMatchPost(MatchPost matchPost) { this.matchPost = matchPost; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    private int matchScore;
    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    private boolean accepted;
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}
