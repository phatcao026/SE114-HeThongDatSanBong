package com.example.timsanbong.data.model;

import java.io.Serializable;

public class Conversation implements Serializable {

    private final String id;
    private final String name;
    private final String initials;
    private final String lastMessage;
    private final String time;
    private final int unreadCount;
    private final String matchContext;
    private final boolean online;

    public Conversation(String id, String name, String initials, String lastMessage,
                        String time, int unreadCount, String matchContext, boolean online) {
        this.id = id;
        this.name = name;
        this.initials = initials;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unreadCount = unreadCount;
        this.matchContext = matchContext;
        this.online = online;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getInitials() { return initials; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
    public String getMatchContext() { return matchContext; }
    public boolean isOnline() { return online; }
}
