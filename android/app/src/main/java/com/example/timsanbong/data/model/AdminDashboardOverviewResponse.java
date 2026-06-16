package com.example.timsanbong.data.model;

import java.math.BigDecimal;

public class AdminDashboardOverviewResponse {
    private long totalUsers;
    private long playerCount;
    private long ownerCount;
    private long adminCount;
    private long totalFields;
    private long availableFields;
    private long maintenanceFields;
    private long bookedFields;
    private long totalBookings;
    private long pendingBookings;
    private long depositPaidBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long completedBookings;
    private long totalPayments;
    private long pendingPayments;
    private long successfulPayments;
    private long failedPayments;
    private long refundedPayments;
    private BigDecimal totalRevenue;
    private long totalMatchPosts;
    private long openMatchPosts;
    private long matchedMatchPosts;
    private long closedMatchPosts;
    private long expiredMatchPosts;
    private long totalReviews;
    private long pendingReviews;
    private long penalizedReviews;
    private long totalConversations;
    private long totalMessages;
    private String generatedAt;

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
    public long getAvailableFields() { return availableFields; }
    public void setAvailableFields(long availableFields) { this.availableFields = availableFields; }
    public long getMaintenanceFields() { return maintenanceFields; }
    public void setMaintenanceFields(long maintenanceFields) { this.maintenanceFields = maintenanceFields; }
    public long getBookedFields() { return bookedFields; }
    public void setBookedFields(long bookedFields) { this.bookedFields = bookedFields; }
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
    public long getDepositPaidBookings() { return depositPaidBookings; }
    public void setDepositPaidBookings(long depositPaidBookings) { this.depositPaidBookings = depositPaidBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }
    public long getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(long completedBookings) { this.completedBookings = completedBookings; }
    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
    public long getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }
    public long getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }
    public long getFailedPayments() { return failedPayments; }
    public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }
    public long getRefundedPayments() { return refundedPayments; }
    public void setRefundedPayments(long refundedPayments) { this.refundedPayments = refundedPayments; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalMatchPosts() { return totalMatchPosts; }
    public void setTotalMatchPosts(long totalMatchPosts) { this.totalMatchPosts = totalMatchPosts; }
    public long getOpenMatchPosts() { return openMatchPosts; }
    public void setOpenMatchPosts(long openMatchPosts) { this.openMatchPosts = openMatchPosts; }
    public long getMatchedMatchPosts() { return matchedMatchPosts; }
    public void setMatchedMatchPosts(long matchedMatchPosts) { this.matchedMatchPosts = matchedMatchPosts; }
    public long getClosedMatchPosts() { return closedMatchPosts; }
    public void setClosedMatchPosts(long closedMatchPosts) { this.closedMatchPosts = closedMatchPosts; }
    public long getExpiredMatchPosts() { return expiredMatchPosts; }
    public void setExpiredMatchPosts(long expiredMatchPosts) { this.expiredMatchPosts = expiredMatchPosts; }
    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
    public long getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(long pendingReviews) { this.pendingReviews = pendingReviews; }
    public long getPenalizedReviews() { return penalizedReviews; }
    public void setPenalizedReviews(long penalizedReviews) { this.penalizedReviews = penalizedReviews; }
    public long getTotalConversations() { return totalConversations; }
    public void setTotalConversations(long totalConversations) { this.totalConversations = totalConversations; }
    public long getTotalMessages() { return totalMessages; }
    public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
