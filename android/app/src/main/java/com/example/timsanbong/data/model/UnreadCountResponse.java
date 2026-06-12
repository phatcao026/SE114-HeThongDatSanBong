package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class UnreadCountResponse {
    @SerializedName("count")
    private int count;

    public int getCount() { return count; }
}
