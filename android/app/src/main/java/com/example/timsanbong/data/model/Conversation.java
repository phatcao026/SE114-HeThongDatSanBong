package com.example.timsanbong.data.model;

import java.io.Serializable;

public class Conversation implements Serializable {

    @com.google.gson.annotations.SerializedName("id")
    private long id;

    @com.google.gson.annotations.SerializedName("otherUser")
    private User otherUser;

    @com.google.gson.annotations.SerializedName("lastMessage")
    private ChatMessage lastMessage;

    @com.google.gson.annotations.SerializedName("unreadCount")
    private int unreadCount;

    // UI Mock backwards compatibility
    private String stringId;
    private String name;
    private String initials;
    private String lastMessageString;
    private String timeAgo;
    private String subtitle;
    private boolean isOnline;

    public Conversation() {}

    // Mock constructor
    public Conversation(String stringId, String name, String initials, String lastMessageString, String timeAgo, int unreadCount, String subtitle, boolean isOnline) {
        this.stringId = stringId;
        this.name = name;
        this.initials = initials;
        this.lastMessageString = lastMessageString;
        this.timeAgo = timeAgo;
        this.unreadCount = unreadCount;
        this.subtitle = subtitle;
        this.isOnline = isOnline;
        try {
            this.id = Long.parseLong(stringId);
        } catch (NumberFormatException ignored) {}
    }

    public Conversation(long id, User otherUser, ChatMessage lastMessage, int unreadCount) {
        this.id = id;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    public String getId() { return stringId != null ? stringId : String.valueOf(id); }
    public User getOtherUser() { return otherUser; }
    public String getLastMessage() { 
        if (lastMessageString != null) return lastMessageString;
        return lastMessage != null ? lastMessage.getContent() : ""; 
    }
    public int getUnreadCount() { return unreadCount; }
    
    public String getName() { return name != null ? name : (otherUser != null ? otherUser.getName() : "Người dùng"); }
    public String getInitials() { return initials != null ? initials : "U"; }
    public String getTimeAgo() { return timeAgo != null ? timeAgo : "Vừa xong"; }
    public String getSubtitle() { return subtitle; }
    public String getMatchContext() { return subtitle; }
    public boolean isOnline() { return isOnline; }
}
