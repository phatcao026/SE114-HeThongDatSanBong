package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MatchPost implements Serializable {

    public static final String TYPE_FIND_OPPONENT = "FIND_OPPONENT";
    public static final String TYPE_FIND_MEMBER = "FIND_MEMBER";

    @SerializedName("id")
    private long id;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userId")
    private long userId;

    @SerializedName("trustScore")
    private Integer trustScore;

    @SerializedName("teamId")
    private long teamId;

    @SerializedName("teamName")
    private String teamName;

    @SerializedName("fieldId")
    private long fieldId;

    @SerializedName("fieldName")
    private String fieldName;

    @SerializedName("bookingId")
    private long bookingId;

    @SerializedName("date")
    private String date;

    @SerializedName("timeStart")
    private String timeStart;

    @SerializedName("timeEnd")
    private String timeEnd;

    @SerializedName("postType")
    private String postType;

    @SerializedName("skillLevel")
    private String skillLevel;

    @SerializedName("costSharing")
    private String costSharing;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status;

    @SerializedName("requestCount")
    private long requestCount;

    @SerializedName("acceptedRequestId")
    private Long acceptedRequestId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("hasField")
    private Boolean hasField;

    @SerializedName("neededMembers")
    private Integer neededMembers;

    @SerializedName("joinedMembers")
    private Integer joinedMembers;

    @SerializedName("targetPositions")
    private String targetPositions;

    @SerializedName("ageRange")
    private String ageRange;

    private boolean accepted;
    private boolean isHot;
    private String captainInitials;
    private String membersSlot;
    private String cost;
    private String timeAgo;

    @SerializedName("team")
    private String team;

    public MatchPost() {}

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
        this.userName = captainName;
        this.captainInitials = captainInitials;
        this.skillLevel = skillLevel;
        this.postType = postType;
        this.fieldName = location;
        this.date = playDate;
        this.timeStart = playTime;
        this.cost = cost;
        this.message = description;
        this.membersSlot = membersSlot;
        this.timeAgo = timeAgo;
        this.isHot = isHot;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getPostType() { return postType; }
    public long getTeamId() { return teamId; }
    public long getFieldId() { return fieldId; }
    public long getBookingId() { return bookingId; }
    public String getSkillLevel() { return skillLevel; }
    public String getDescription() { return message; }
    public String getPlayDate() { return date; }
    public String getPlayTime() { return getTime(); }
    public String getLocation() { return fieldName; }
    public String getStatus() { return status; }
    public Boolean getHasField() { return hasField; }
    public Integer getNeededMembers() { return neededMembers; }
    public Integer getJoinedMembers() { return joinedMembers; }
    public String getTargetPositions() { return targetPositions; }
    public String getAgeRange() { return ageRange; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getCaptainName() { return userName; }
    public void setCaptainName(String captainName) { this.userName = captainName; }
    public int getTrustScore() { return trustScore != null ? trustScore : 100; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }

    public String getIdString() { return String.valueOf(id); }
    public String getType() { return postType != null ? postType : TYPE_FIND_OPPONENT; }
    public String getTypeLabel() {
        if (TYPE_FIND_MEMBER.equals(postType)) return "Tìm cầu thủ";
        return "Tìm đối thủ";
    }
    public String getTeam() {
        if (teamName != null && !teamName.trim().isEmpty()) return teamName;
        if (team != null && !team.trim().isEmpty()) return team;
        return "Đội bóng";
    }
    public String getCaptain() { return userName != null ? userName : "Đội trưởng"; }
    public String getCaptainInitials() {
        if (captainInitials != null) return captainInitials;
        String captain = getCaptain();
        if (captain.isEmpty()) return "C";
        String[] parts = captain.trim().split("\\s+");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return captain.substring(0, Math.min(captain.length(), 2)).toUpperCase();
    }
    public String getLevel() { return skillLevel != null ? skillLevel : "INTERMEDIATE"; }
    public String getDate() { return date != null ? date : ""; }
    public String getTime() {
        if (timeStart == null || timeStart.isEmpty()) return "";
        String start = timeStart.length() > 5 ? timeStart.substring(0, 5) : timeStart;
        if (timeEnd == null || timeEnd.isEmpty()) return start;
        String end = timeEnd.length() > 5 ? timeEnd.substring(0, 5) : timeEnd;
        return start + " - " + end;
    }
    public String getField() { return fieldName != null ? fieldName : "Sân bóng"; }
    public String getMembersSlot() {
        if (TYPE_FIND_MEMBER.equals(postType)) {
            return (neededMembers != null ? neededMembers : 0) + " thành viên";
        }
        return requestCount + " yêu cầu";
    }
    public String getCost() {
        if (cost != null) return cost;
        return costSharing != null ? costSharing : "";
    }
    public String getMessage() { return message != null ? message : ""; }
    public String getTimeAgo() {
        if (timeAgo != null) return timeAgo;
        if (createdAt == null || createdAt.isEmpty()) return "Vừa xong";
        try {
            // Server format: 2026-06-20T16:02:28.868441
            String cleanDate = createdAt.split("\\.")[0].replace("T", " ");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
            java.util.Date createdDate = sdf.parse(cleanDate);
            long diff = System.currentTimeMillis() - createdDate.getTime();
            long minutes = diff / (60 * 1000);
            if (minutes < 1) return "Vừa xong";
            if (minutes < 60) return minutes + " phút trước";
            long hours = minutes / 60;
            if (hours < 24) return hours + " giờ trước";
            return (hours / 24) + " ngày trước";
        } catch (Exception e) {
            return createdAt;
        }
    }
    public String getPostedAgo() { return getTimeAgo(); }
    public boolean isHot() { return isHot || requestCount > 0; }
    public boolean isAccepted() { return accepted || acceptedRequestId != null; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}
