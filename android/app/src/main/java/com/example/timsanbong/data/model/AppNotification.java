package com.example.timsanbong.data.model;

public class AppNotification {

    private final int iconRes;
    private final int iconTintRes;
    private final String title;
    private final String body;
    private final String time;

    public AppNotification(int iconRes, int iconTintRes, String title, String body, String time) {
        this.iconRes = iconRes;
        this.iconTintRes = iconTintRes;
        this.title = title;
        this.body = body;
        this.time = time;
    }

    public int getIconRes() { return iconRes; }
    public int getIconTintRes() { return iconTintRes; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getTime() { return time; }
}
