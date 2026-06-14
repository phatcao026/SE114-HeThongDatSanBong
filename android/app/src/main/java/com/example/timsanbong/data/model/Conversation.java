package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Conversation implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("otherUser")
    private User otherUser;

    @SerializedName("lastMessage")
    private ChatMessage lastMessage;

    @SerializedName("unreadCount")
    private int unreadCount;

    @SerializedName("name")
    private String apiName;

    @SerializedName("memberNames")
    private List<String> memberNames;

    @SerializedName("lastMessageContent")
    private String lastMessageContent;

    @SerializedName("lastMessageCreatedAt")
    private String lastMessageCreatedAt;

    @SerializedName("createdAt")
    private String createdAt;

    private String stringId;
    private String name;
    private String initials;
    private String lastMessageString;
    private String timeAgo;
    private String subtitle;
    private boolean isOnline;

    public Conversation() {}

    public Conversation(String stringId, String name, String initials, String lastMessageString,
                        String timeAgo, int unreadCount, String subtitle, boolean isOnline) {
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

    public String getId() {
        return stringId != null ? stringId : String.valueOf(id);
    }

    public User getOtherUser() {
        return otherUser;
    }

    public String getLastMessage() {
        if (lastMessageString != null) return lastMessageString;
        if (lastMessageContent != null) return lastMessageContent;
        return lastMessage != null ? lastMessage.getContent() : "";
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public String getName() {
        if (name != null) return name;
        if (apiName != null) return apiName;
        if (otherUser != null) return otherUser.getName();
        if (memberNames != null && !memberNames.isEmpty()) return memberNames.get(0);
        return "Nguoi dung";
    }

    public String getInitials() {
        if (initials != null) return initials;
        String displayName = getName();
        return displayName.isEmpty() ? "U" : displayName.substring(0, 1).toUpperCase();
    }

    public String getTimeAgo() {
        if (timeAgo != null) return timeAgo;
        if (lastMessageCreatedAt != null) return lastMessageCreatedAt;
        if (createdAt != null) return createdAt;
        return "Vua xong";
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getMatchContext() {
        return subtitle;
    }

    public boolean isOnline() {
        return isOnline;
    }
}
