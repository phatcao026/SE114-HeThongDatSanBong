package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.time.LocalDateTime;

public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private Long captainId;
    private String captainName;
    private Enums.TeamLevel level;
    private Boolean isCaptain;
    private Enums.TeamMemberStatus memberStatus;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCaptainId() {
        return captainId;
    }

    public void setCaptainId(Long captainId) {
        this.captainId = captainId;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public Enums.TeamLevel getLevel() {
        return level;
    }

    public void setLevel(Enums.TeamLevel level) {
        this.level = level;
    }

    public Boolean getIsCaptain() {
        return isCaptain;
    }

    public void setIsCaptain(Boolean captain) {
        isCaptain = captain;
    }

    public Enums.TeamMemberStatus getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(Enums.TeamMemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
