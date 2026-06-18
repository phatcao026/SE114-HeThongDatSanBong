package com.example.backend.dto.ai;

public class AiOpponentDto {
    private Long matchId;
    private String playStyleNote;
    private int trustScore;
    private String date;
    private String timeStart;
    private String timeEnd;
    private String skillLevel;
    private String costSharing;
    private String ageRange;
    private Boolean hasField;
    private String targetPositions;

    public AiOpponentDto() {}

    public AiOpponentDto(Long matchId, String playStyleNote, int trustScore, String date,
                         String timeStart, String timeEnd, String skillLevel, String costSharing,
                         String ageRange, Boolean hasField, String targetPositions) {
        this.matchId = matchId;
        this.playStyleNote = playStyleNote;
        this.trustScore = trustScore;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.skillLevel = skillLevel;
        this.costSharing = costSharing;
        this.ageRange = ageRange;
        this.hasField = hasField;
        this.targetPositions = targetPositions;
    }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getPlayStyleNote() { return playStyleNote; }
    public void setPlayStyleNote(String playStyleNote) { this.playStyleNote = playStyleNote; }

    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTimeStart() { return timeStart; }
    public void setTimeStart(String timeStart) { this.timeStart = timeStart; }

    public String getTimeEnd() { return timeEnd; }
    public void setTimeEnd(String timeEnd) { this.timeEnd = timeEnd; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getCostSharing() { return costSharing; }
    public void setCostSharing(String costSharing) { this.costSharing = costSharing; }

    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }

    public Boolean getHasField() { return hasField; }
    public void setHasField(Boolean hasField) { this.hasField = hasField; }

    public String getTargetPositions() { return targetPositions; }
    public void setTargetPositions(String targetPositions) { this.targetPositions = targetPositions; }

    @Override
    public String toString() {
        return "AiOpponentDto{" +
                "matchId=" + matchId +
                ", playStyleNote='" + playStyleNote + '\'' +
                ", trustScore=" + trustScore +
                ", date='" + date + '\'' +
                ", timeStart='" + timeStart + '\'' +
                ", timeEnd='" + timeEnd + '\'' +
                ", skillLevel='" + skillLevel + '\'' +
                ", costSharing='" + costSharing + '\'' +
                ", ageRange='" + ageRange + '\'' +
                ", hasField=" + hasField +
                ", targetPositions='" + targetPositions + '\'' +
                '}';
    }
}
