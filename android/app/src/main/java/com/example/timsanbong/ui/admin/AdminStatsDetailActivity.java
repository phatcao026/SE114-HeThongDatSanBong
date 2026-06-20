package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AdminDashboardOverviewResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStatsDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        fetchDetailedStats();
    }

    private void fetchDetailedStats() {
        ApiClient.getService(this).getAdminOverview().enqueue(new Callback<AdminDashboardOverviewResponse>() {
            @Override
            public void onResponse(Call<AdminDashboardOverviewResponse> call, Response<AdminDashboardOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(AdminStatsDetailActivity.this, "Không thể tải báo cáo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminDashboardOverviewResponse> call, Throwable t) {
                Toast.makeText(AdminStatsDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(AdminDashboardOverviewResponse data) {
        // Users
        setupItem(findViewById(R.id.statPlayers), "Người chơi", String.valueOf(data.getPlayerCount()), "PLAYER");
        setupItem(findViewById(R.id.statOwners), "Chủ sân", String.valueOf(data.getOwnerCount()), "OWNER");
        setupItem(findViewById(R.id.statAdmins), "Quản trị viên", String.valueOf(data.getAdminCount()), "ADMIN");

        // Bookings
        setupItem(findViewById(R.id.statBookingsPending), "Chờ xử lý", String.valueOf(data.getPendingBookings()), "Pending");
        setupItem(findViewById(R.id.statBookingsConfirmed), "Đã xác nhận", String.valueOf(data.getConfirmedBookings()), "Confirmed");
        setupItem(findViewById(R.id.statBookingsCompleted), "Hoàn thành", String.valueOf(data.getCompletedBookings()), "Done");
        setupItem(findViewById(R.id.statBookingsCancelled), "Đã hủy", String.valueOf(data.getCancelledBookings()), "Cancelled");

        // Fields
        setupItem(findViewById(R.id.statFieldsAvailable), "Đang hoạt động", String.valueOf(data.getAvailableFields()), "Active");
        setupItem(findViewById(R.id.statFieldsMaintenance), "Bảo trì", String.valueOf(data.getMaintenanceFields()), "Maint");
        setupItem(findViewById(R.id.statFieldsBooked), "Đã được đặt", String.valueOf(data.getBookedFields()), "Booked");
    }

    private void setupItem(View view, String label, String value, String trend) {
        ((TextView) view.findViewById(R.id.tvHealthLabel)).setText(label);
        ((TextView) view.findViewById(R.id.tvHealthValue)).setText(value);
        ((TextView) view.findViewById(R.id.tvHealthTrend)).setText(trend);
    }
}
