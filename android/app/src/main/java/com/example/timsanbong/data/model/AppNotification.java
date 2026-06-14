package com.example.timsanbong.data.model;

import com.example.timsanbong.R;
import com.google.gson.annotations.SerializedName;

public class AppNotification {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName(value = "body", alternate = {"content"})
    private String body;

    @SerializedName(value = "read", alternate = {"isRead"})
    private boolean read;

    @SerializedName("type")
    private String type;

    @SerializedName("createdAt")
    private String createdAt;

    public AppNotification() {}

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
    public String getType() { return type; }
    public String getCreatedAt() { return createdAt; }

    public void setRead(boolean read) { this.read = read; }

    public int getIconRes() { return R.drawable.ic_bell; }
    public int getIconTintRes() { return R.color.primary; }
    public String getTime() { return createdAt != null ? createdAt : ""; }
}
