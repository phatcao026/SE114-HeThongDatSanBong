package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class MatchRequestResponse {
    @SerializedName("id")
    private long id;

    @SerializedName(value = "matchPostId", alternate = {"postId"})
    private long matchPostId;

    @SerializedName("requesterId")
    private long requesterId;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() { return id; }
    public long getMatchPostId() { return matchPostId; }
    public long getRequesterId() { return requesterId; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
