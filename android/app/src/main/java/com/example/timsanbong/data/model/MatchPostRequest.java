package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class MatchPostRequest {
    @SerializedName("postType")
    private String postType;

    @SerializedName("teamId")
    private Long teamId;

    @SerializedName("fieldId")
    private Long fieldId;

    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("date")
    private String date;

    @SerializedName("timeStart")
    private String timeStart;

    @SerializedName("timeEnd")
    private String timeEnd;

    @SerializedName("skillLevel")
    private String skillLevel;

    @SerializedName("costSharing")
    private String costSharing;

    @SerializedName("message")
    private String message;

    @SerializedName("hasField")
    private Boolean hasField;

    @SerializedName("neededMembers")
    private Integer neededMembers;

    @SerializedName("targetPositions")
    private String targetPositions;

    @SerializedName("ageRange")
    private String ageRange;

    @SerializedName("teamName")
    private String teamName;
    
    @SerializedName("team")
    private String team;

    @SerializedName("fieldName")
    private String fieldName;

    public MatchPostRequest() {}

    public void setPostType(String postType) { this.postType = postType; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public void setTeamName(String teamName) { this.teamName = teamName; this.team = teamName; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public void setDate(String date) { this.date = date; }
    public void setTimeStart(String timeStart) { this.timeStart = timeStart; }
    public void setTimeEnd(String timeEnd) { this.timeEnd = timeEnd; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public void setCostSharing(String costSharing) { this.costSharing = costSharing; }
    public void setMessage(String message) { this.message = message; }
    public void setHasField(Boolean hasField) { this.hasField = hasField; }
    public void setNeededMembers(Integer neededMembers) { this.neededMembers = neededMembers; }
    public void setTargetPositions(String targetPositions) { this.targetPositions = targetPositions; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }

    public void setDescription(String description) { this.message = description; }
    public void setPlayDate(String playDate) { this.date = playDate; }
    public void setLocation(String location) { this.fieldName = location; }
}
