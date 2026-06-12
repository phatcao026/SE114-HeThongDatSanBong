package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class TeamResponse {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("level")
    private String level;

    @SerializedName("description")
    private String description;

    @SerializedName("memberCount")
    private int memberCount;

    public long getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }
    public String getDescription() { return description; }
    public int getMemberCount() { return memberCount; }
}
