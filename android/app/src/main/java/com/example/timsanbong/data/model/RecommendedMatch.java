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
}
