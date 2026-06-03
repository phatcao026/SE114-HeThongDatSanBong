package com.example.backend.entity;

import com.example.backend.utils.Enums;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "match_posts")
public class MatchPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "booking_id")
    private Long bookingId;

    private LocalDate date;
    @Column(name = "time_start")
    private LocalTime timeStart;
    @Column(name = "time_end")
    private LocalTime timeEnd;

    @Enumerated(EnumType.STRING)
    private Enums.PostType postType;

    @Enumerated(EnumType.STRING)
    private Enums.TeamLevel skillLevel;

    @Column(name = "cost_sharing")
    private String costSharing;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private Enums.PostStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "field_id", insertable = false, updatable = false)
    private Field field;

    @ManyToOne
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<MatchRequest> requests;

    public MatchPost() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTimeStart() { return timeStart; }
    public void setTimeStart(LocalTime timeStart) { this.timeStart = timeStart; }

    public LocalTime getTimeEnd() { return timeEnd; }
    public void setTimeEnd(LocalTime timeEnd) { this.timeEnd = timeEnd; }

    public Enums.PostType getPostType() { return postType; }
    public void setPostType(Enums.PostType postType) { this.postType = postType; }

    public Enums.TeamLevel getSkillLevel() { return skillLevel; }
    public void setSkillLevel(Enums.TeamLevel skillLevel) { this.skillLevel = skillLevel; }

    public String getCostSharing() { return costSharing; }
    public void setCostSharing(String costSharing) { this.costSharing = costSharing; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Enums.PostStatus getStatus() { return status; }
    public void setStatus(Enums.PostStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<MatchRequest> getRequests() { return requests; }
    public void setRequests(List<MatchRequest> requests) { this.requests = requests; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Field getField() { return field; }
    public void setField(Field field) { this.field = field; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}
