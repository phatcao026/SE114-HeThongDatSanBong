package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class TeamRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("level")
    private String level;

    @SerializedName("description")
    private String description;

    public TeamRequest() {}
    
    public TeamRequest(String name, String level, String description) {
        this.name = name;
        this.level = level;
        this.description = description;
    }

    public void setName(String name) { this.name = name; }
    public void setSkillLevel(String level) { this.level = level; }
}
