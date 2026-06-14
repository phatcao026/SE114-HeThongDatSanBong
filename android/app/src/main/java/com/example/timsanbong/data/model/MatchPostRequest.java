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

    public MatchPostRequest() {}

    public void setPostType(String postType) { this.postType = postType; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public void setDate(String date) { this.date = date; }
    public void setTimeStart(String timeStart) { this.timeStart = timeStart; }
    public void setTimeEnd(String timeEnd) { this.timeEnd = timeEnd; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public void setCostSharing(String costSharing) { this.costSharing = costSharing; }
    public void setMessage(String message) { this.message = message; }

    public void setDescription(String description) { this.message = description; }
    public void setPlayDate(String playDate) { this.date = playDate; }
    public void setLocation(String location) {}
}
