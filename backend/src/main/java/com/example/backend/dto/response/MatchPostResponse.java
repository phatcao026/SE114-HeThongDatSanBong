package com.example.backend.dto.response;

import com.example.backend.utils.Enums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MatchPostResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long fieldId;
    private String fieldName;
    private Long bookingId;
    private LocalDate date;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private Enums.PostType postType;
    private Enums.TeamLevel skillLevel;
    private String costSharing;
    private String message;
    private Enums.PostStatus status;
    private long requestCount;
    private Long acceptedRequestId;
    private LocalDateTime createdAt;
    private Integer neededMembers;
    private Integer joinedMembers;
    private Long conversationId;
    private Boolean hasField;
    private String targetPositions;
    private String ageRange;
    private String teamName;
    private Integer trustScore;
    private Integer matchesPlayed;
    private Integer noShows;
    private Double averageRating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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

    public Enums.PostStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.PostStatus status) {
        this.status = status;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public Long getAcceptedRequestId() {
        return acceptedRequestId;
    }

    public void setAcceptedRequestId(Long acceptedRequestId) {
        this.acceptedRequestId = acceptedRequestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getNeededMembers() {
        return neededMembers;
    }

    public void setNeededMembers(Integer neededMembers) {
        this.neededMembers = neededMembers;
    }

    public Integer getJoinedMembers() {
        return joinedMembers;
    }

    public void setJoinedMembers(Integer joinedMembers) {
        this.joinedMembers = joinedMembers;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
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

    public Integer getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Integer trustScore) {
        this.trustScore = trustScore;
    }

    public Integer getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(Integer matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public Integer getNoShows() {
        return noShows;
    }

    public void setNoShows(Integer noShows) {
        this.noShows = noShows;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
