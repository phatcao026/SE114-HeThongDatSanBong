package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("userId")
    private long userId;

    @SerializedName("role")
    private String role;

    @SerializedName("email")
    private String email;

    public String getAccessToken() { return accessToken; }
    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
}
