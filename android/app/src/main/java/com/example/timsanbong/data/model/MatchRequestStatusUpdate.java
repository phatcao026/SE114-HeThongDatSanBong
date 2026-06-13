package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class MatchRequestStatusUpdate {
    @SerializedName("status")
    private String status;

    public MatchRequestStatusUpdate(String status) {
        this.status = status;
    }
}
