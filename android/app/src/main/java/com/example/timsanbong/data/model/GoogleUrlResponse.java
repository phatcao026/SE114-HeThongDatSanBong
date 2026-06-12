package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class GoogleUrlResponse {
    @SerializedName("url")
    private String url;

    public String getUrl() { return url; }
}
