package com.example.backend.dto.request;

import com.example.backend.utils.Enums;

import java.time.LocalDate;
import java.time.LocalTime;

public class MatchPostCreateRequest {
    private Long fieldId;
    private Long bookingId;
    private LocalDate date;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private Enums.PostType postType;
    private Enums.TeamLevel skillLevel;
    private String costSharing;
    private String message;
    private Integer neededMembers;
    private Boolean hasField;
    private String targetPositions;
    private String ageRange;
    private String teamName;
    private Long teamId;

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(LocalTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    public Enums.PostType getPostType() {
        return postType;
    }

    public void setPostType(Enums.PostType postType) {
        this.postType = postType;
    }

    public Enums.TeamLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Enums.TeamLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getCostSharing() {
        return costSharing;
    }

    public void setCostSharing(String costSharing) {
        this.costSharing = costSharing;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getNeededMembers() {
        return neededMembers;
    }

    public void setNeededMembers(Integer neededMembers) {
        this.neededMembers = neededMembers;
    }

    public Boolean getHasField() {
        return hasField;
    }

    public void setHasField(Boolean hasField) {
        this.hasField = hasField;
    }

    public String getTargetPositions() {
        return targetPositions;
    }

    public void setTargetPositions(String targetPositions) {
        this.targetPositions = targetPositions;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
