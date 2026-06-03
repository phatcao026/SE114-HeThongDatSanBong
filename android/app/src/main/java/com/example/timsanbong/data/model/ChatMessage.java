package com.example.timsanbong.data.model;

public class ChatMessage {

    public static final String SIDE_ME = "me";
    public static final String SIDE_THEM = "them";
    public static final String SIDE_SYSTEM = "system";

    private final String side;
    private final String text;
    private final String time;

    public ChatMessage(String side, String text, String time) {
        this.side = side;
        this.text = text;
        this.time = time;
    }

    public String getSide() { return side; }
    public String getText() { return text; }
    public String getTime() { return time; }
}
