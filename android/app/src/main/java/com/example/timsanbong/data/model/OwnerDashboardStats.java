package com.example.timsanbong.data.model;

import java.util.List;
import java.util.Locale;

public class OwnerDashboardStats {
    private final int totalFields;
    private final int pendingBookings;
    private final int confirmedBookings;
    private final int completedBookings;
    private final double completedRevenue;

    public OwnerDashboardStats(int totalFields, int pendingBookings, int confirmedBookings,
                               int completedBookings, double completedRevenue) {
        this.totalFields = totalFields;
        this.pendingBookings = pendingBookings;
        this.confirmedBookings = confirmedBookings;
        this.completedBookings = completedBookings;
        this.completedRevenue = completedRevenue;
    }

    public static OwnerDashboardStats from(List<Field> fields, List<Booking> bookings) {
        int totalFields = fields == null ? 0 : fields.size();
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        double revenue = 0;

        if (bookings != null) {
            for (Booking booking : bookings) {
                String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
                if ("PENDING".equals(status) || "DEPOSIT_PAID".equals(status)) {
                    pending++;
                } else if ("CONFIRMED".equals(status)) {
                    confirmed++;
                } else if ("COMPLETED".equals(status)) {
                    completed++;
                    revenue += booking.getTotalPrice();
                }
            }
        }

        return new OwnerDashboardStats(totalFields, pending, confirmed, completed, revenue);
    }

    public int getTotalFields() {
        return totalFields;
    }

    public int getPendingBookings() {
        return pendingBookings;
    }

    public int getConfirmedBookings() {
        return confirmedBookings;
    }

    public int getCompletedBookings() {
        return completedBookings;
    }

    public double getCompletedRevenue() {
        return completedRevenue;
    }
}
