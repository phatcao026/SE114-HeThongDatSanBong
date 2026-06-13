package com.example.timsanbong.data.model;

public class AppNotification {

    @com.google.gson.annotations.SerializedName("id")
    private long id;

    @com.google.gson.annotations.SerializedName("title")
    private String title;

    @com.google.gson.annotations.SerializedName("body")
    private String body;

    @com.google.gson.annotations.SerializedName("read")
    private boolean read;

    @com.google.gson.annotations.SerializedName("createdAt")
    private String createdAt;

    private int iconRes;
    private int iconTintRes;

    public AppNotification() {}

    // Mock constructor
    public AppNotification(int iconRes, int iconTintRes, String title, String body, String time) {
        this.iconRes = iconRes;
        this.iconTintRes = iconTintRes;
        this.title = title;
        this.body = body;
        this.createdAt = time;
    }

    public AppNotification(long id, String title, String body, boolean read, String createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.read = read;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isRead() { return read; }
    public String getCreatedAt() { return createdAt; }
    
    public void setRead(boolean read) { this.read = read; }
    
    public int getIconRes() { return iconRes != 0 ? iconRes : com.example.timsanbong.R.drawable.ic_bell; }
    public int getIconTintRes() { return iconTintRes != 0 ? iconTintRes : com.example.timsanbong.R.color.primary; }
    public String getTime() { return createdAt != null ? createdAt : "Vừa xong"; }
}
