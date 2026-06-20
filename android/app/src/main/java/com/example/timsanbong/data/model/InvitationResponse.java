package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class InvitationResponse {
    @SerializedName("id")
    private long id;

    @SerializedName("teamId")
    private long teamId;

    @SerializedName("teamName")
    private String teamName;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() { return id; }
    public long getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public String getTeam() { return (teamName != null && !teamName.trim().isEmpty()) ? teamName : "Đội bóng"; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
