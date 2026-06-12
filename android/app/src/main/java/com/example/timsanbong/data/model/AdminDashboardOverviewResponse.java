package com.example.timsanbong.data.model;

import java.math.BigDecimal;

public class AdminDashboardOverviewResponse {
    private long totalUsers;
    private long playerCount;
    private long ownerCount;
    private long adminCount;
    private long totalFields;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private long totalMatchPosts;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getPlayerCount() { return playerCount; }
    public void setPlayerCount(long playerCount) { this.playerCount = playerCount; }
    public long getOwnerCount() { return ownerCount; }
    public void setOwnerCount(long ownerCount) { this.ownerCount = ownerCount; }
    public long getAdminCount() { return adminCount; }
    public void setAdminCount(long adminCount) { this.adminCount = adminCount; }
    public long getTotalFields() { return totalFields; }
    public void setTotalFields(long totalFields) { this.totalFields = totalFields; }
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalMatchPosts() { return totalMatchPosts; }
    public void setTotalMatchPosts(long totalMatchPosts) { this.totalMatchPosts = totalMatchPosts; }
}
