package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class MatchPostRequest {
    @SerializedName("postType")
    private String postType;

    @SerializedName("teamId")
    private long teamId;

    @SerializedName("skillLevel")
    private String skillLevel;

    @SerializedName("description")
    private String description;

    @SerializedName("playDate")
    private String playDate;

    @SerializedName("playTime")
    private String playTime;

    @SerializedName("location")
    private String location;

    public MatchPostRequest() {}

    public MatchPostRequest(String postType, long teamId, String skillLevel, String description, String playDate, String playTime, String location) {
        this.postType = postType;
        this.teamId = teamId;
        this.skillLevel = skillLevel;
        this.description = description;
        this.playDate = playDate;
        this.playTime = playTime;
        this.location = location;
    }

    public void setPostType(String postType) { this.postType = postType; }
    public void setTeamId(long teamId) { this.teamId = teamId; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public void setDescription(String description) { this.description = description; }
    public void setPlayDate(String playDate) { this.playDate = playDate; }
    public void setPlayTime(String playTime) { this.playTime = playTime; }
    public void setLocation(String location) { this.location = location; }
}
