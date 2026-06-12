package com.example.timsanbong.data.model;

import java.io.Serializable;

public class MatchPost implements Serializable {

    public static final String TYPE_FIND_OPPONENT = "FIND_OPPONENT";
    public static final String TYPE_FIND_MEMBER = "FIND_MEMBER";

    @com.google.gson.annotations.SerializedName("id")
    private long id;

    @com.google.gson.annotations.SerializedName("postType")
    private String postType;

    @com.google.gson.annotations.SerializedName("teamId")
    private long teamId;

    @com.google.gson.annotations.SerializedName("skillLevel")
    private String skillLevel;

    @com.google.gson.annotations.SerializedName("description")
    private String description;

    @com.google.gson.annotations.SerializedName("playDate")
    private String playDate;

    @com.google.gson.annotations.SerializedName("playTime")
    private String playTime;

    @com.google.gson.annotations.SerializedName("location")
    private String location;

    // Optional fields that might come from extended API response or populated later
    @com.google.gson.annotations.SerializedName("teamName")
    private String teamName;

    @com.google.gson.annotations.SerializedName("captainName")
    private String captainName;

    @com.google.gson.annotations.SerializedName("trustScore")
    private int trustScore;

    // UI-only properties for backward compatibility with mock data
    private boolean accepted;
    private boolean isHot;
    private String membersSlot;
    private String cost;
    private String timeAgo;

    public MatchPost() {}

    // Constructor for backward compatibility with mocks
    public MatchPost(String stringId, String teamName, String captainName, String captainInitials,
                     int trustScore, String skillLevel, String postType, String typeLabel,
                     String location, String playDate, String playTime, String cost,
                     String description, String membersSlot, String timeAgo, boolean isHot) {
        try {
            this.id = Long.parseLong(stringId);
        } catch (NumberFormatException e) {
            this.id = 0;
        }
        this.teamName = teamName;
        this.captainName = captainName;
        this.trustScore = trustScore;
        this.skillLevel = skillLevel;
        this.postType = postType;
        this.location = location;
        this.playDate = playDate;
        this.playTime = playTime;
        this.cost = cost;
        this.description = description;
        this.membersSlot = membersSlot;
        this.timeAgo = timeAgo;
        this.isHot = isHot;
    }

    public MatchPost(long id, String postType, long teamId, String skillLevel, String description, String playDate, String playTime, String location) {
        this.id = id;
        this.postType = postType;
        this.teamId = teamId;
        this.skillLevel = skillLevel;
        this.description = description;
        this.playDate = playDate;
        this.playTime = playTime;
        this.location = location;
    }

    public long getId() { return id; }
    public String getPostType() { return postType; }
    public long getTeamId() { return teamId; }
    public String getSkillLevel() { return skillLevel; }
    public String getDescription() { return description; }
    public String getPlayDate() { return playDate; }
    public String getPlayTime() { return playTime; }
    public String getLocation() { return location; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public String getCaptainName() { return captainName; }
    public void setCaptainName(String captainName) { this.captainName = captainName; }
    
    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }

    // Methods for UI compatibility
    public String getIdString() { return String.valueOf(id); }
    public String getType() { return postType != null ? postType : TYPE_FIND_OPPONENT; }
    public String getTypeLabel() {
        if (TYPE_FIND_MEMBER.equals(postType)) return "Tìm cầu thủ";
        return "Tìm đối";
    }
    public String getTeam() { return teamName != null ? teamName : "Đội bóng"; }
    public String getCaptain() { return captainName != null ? captainName : "Đội trưởng"; }
    public String getCaptainInitials() {
        if (captainName != null && !captainName.isEmpty()) {
            return String.valueOf(captainName.charAt(0)).toUpperCase();
        }
        return "C";
    }
    public String getLevel() { return skillLevel != null ? skillLevel : "Amateur"; }
    public String getDate() { return playDate != null ? playDate : "14/06"; }
    public String getTime() { return playTime != null ? playTime : "18:00"; }
    public String getField() { return location != null ? location : "Sân bóng"; }
    public String getMembersSlot() { return membersSlot != null ? membersSlot : "5/7"; }
    public String getCost() { return cost != null ? cost : "0đ"; }
    public String getMessage() { return description != null ? description : ""; }
    public String getTimeAgo() { return timeAgo != null ? timeAgo : "Vừa xong"; }
    public String getPostedAgo() { return getTimeAgo(); }
    public boolean isHot() { return isHot; }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}
