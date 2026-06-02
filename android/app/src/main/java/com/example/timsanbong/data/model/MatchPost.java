package com.example.timsanbong.data.model;

import java.io.Serializable;

public class MatchPost implements Serializable {

    public static final String TYPE_FIND_OPPONENT = "FIND_OPPONENT";
    public static final String TYPE_FIND_MEMBER = "FIND_MEMBER";

    private final String id;
    private final String team;
    private final String captain;
    private final String captainInitials;
    private final int trustScore;
    private final String level;
    private final String type;
    private final String typeLabel;
    private final String field;
    private final String date;
    private final String time;
    private final String cost;
    private final String message;
    private final String membersSlot;
    private final String postedAgo;
    private final boolean hot;
    private boolean accepted;

    public MatchPost(String id, String team, String captain, String captainInitials,
                     int trustScore, String level, String type, String typeLabel,
                     String field, String date, String time, String cost,
                     String message, String membersSlot, String postedAgo, boolean hot) {
        this.id = id;
        this.team = team;
        this.captain = captain;
        this.captainInitials = captainInitials;
        this.trustScore = trustScore;
        this.level = level;
        this.type = type;
        this.typeLabel = typeLabel;
        this.field = field;
        this.date = date;
        this.time = time;
        this.cost = cost;
        this.message = message;
        this.membersSlot = membersSlot;
        this.postedAgo = postedAgo;
        this.hot = hot;
        this.accepted = false;
    }

    public String getId() { return id; }
    public String getTeam() { return team; }
    public String getCaptain() { return captain; }
    public String getCaptainInitials() { return captainInitials; }
    public int getTrustScore() { return trustScore; }
    public String getLevel() { return level; }
    public String getType() { return type; }
    public String getTypeLabel() { return typeLabel; }
    public String getField() { return field; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getCost() { return cost; }
    public String getMessage() { return message; }
    public String getMembersSlot() { return membersSlot; }
    public String getPostedAgo() { return postedAgo; }
    public boolean isHot() { return hot; }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}
