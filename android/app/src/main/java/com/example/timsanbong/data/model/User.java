package com.example.timsanbong.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private long id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("trustScore")
    private int trustScore;

    @SerializedName("isLocked")
    private boolean isLocked;

    public User() {}

    public User(long id, String fullName, String email, String phone, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public int getTrustScore() { return trustScore; }
    public boolean isLocked() { return isLocked; }

    public void setId(long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }
    public void setLocked(boolean locked) { isLocked = locked; }
}
