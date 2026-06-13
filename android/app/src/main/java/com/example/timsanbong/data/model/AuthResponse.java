package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName(value = "accessToken", alternate = {"token"})
    private String accessToken;

    @SerializedName("message")
    private String message;

    @SerializedName("userId")
    private long userId;

    @SerializedName("role")
    private String role;

    @SerializedName("email")
    private String email;

    public String getAccessToken() { return accessToken; }
    public String getToken() { return accessToken; }
    public String getMessage() { return message; }
    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
}
