package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AdminDashboardOverviewResponse;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMainActivity extends AppCompatActivity {

    private AdminNavBarManager navBarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        
        fetchStats();
        setupHealth();
        
        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });
        
        navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_OVERVIEW);
        navBarManager.setup();

        findViewById(R.id.cardGmv).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminTransactionActivity.class));
        });

        findViewById(R.id.cardUsers).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUserActivity.class));
        });

        findViewById(R.id.cardFields).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminFieldActivity.class));
        });

        findViewById(R.id.cardBookings).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBookingActivity.class));
        });

        findViewById(R.id.cardAdminMatchPosts).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminMatchPostActivity.class));
        });

        findViewById(R.id.cardAdminReviews).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminReviewActivity.class));
        });

        findViewById(R.id.btnViewAudit).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAuditActivity.class));
        });
    }

    private void fetchStats() {
        ApiClient.getService(this).getAdminOverview().enqueue(new Callback<AdminDashboardOverviewResponse>() {
            @Override
            public void onResponse(Call<AdminDashboardOverviewResponse> call, Response<AdminDashboardOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(AdminMainActivity.this, "Không thể tải dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                    setupDefaultStats();
                }
            }

            @Override
            public void onFailure(Call<AdminDashboardOverviewResponse> call, Throwable t) {
                Toast.makeText(AdminMainActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                setupDefaultStats();
            }
        });
    }

    private void updateUI(AdminDashboardOverviewResponse stats) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        double revenue = stats.getTotalRevenue() != null ? stats.getTotalRevenue().doubleValue() : 0;
        String formattedRevenue = currencyFormat.format(revenue);

        // Big GMV Card
        ((TextView) findViewById(R.id.tvWeeklyGmv)).setText(formattedRevenue);

        // Stats Cards
        View gmv = findViewById(R.id.cardGmv);
        ((ImageView) gmv.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_wallet);
        ((TextView) gmv.findViewById(R.id.tvStatLabel)).setText("Doanh thu");
        ((TextView) gmv.findViewById(R.id.tvStatValue)).setText(formattedRevenue);
        ((TextView) gmv.findViewById(R.id.tvTrendValue)).setText("Tổng GMV");

        View users = findViewById(R.id.cardUsers);
        ((ImageView) users.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_users);
        ((TextView) users.findViewById(R.id.tvStatLabel)).setText("Người dùng");
        ((TextView) users.findViewById(R.id.tvStatValue)).setText(String.valueOf(stats.getTotalUsers()));
        ((TextView) users.findViewById(R.id.tvTrendValue)).setText("Tổng số");

        View fields = findViewById(R.id.cardFields);
        ((ImageView) fields.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_stadium);
        ((TextView) fields.findViewById(R.id.tvStatLabel)).setText("Sân bóng");
        ((TextView) fields.findViewById(R.id.tvStatValue)).setText(String.valueOf(stats.getTotalFields()));
        ((TextView) fields.findViewById(R.id.tvTrendValue)).setText("Tổng số");

        View bookings = findViewById(R.id.cardBookings);
        ((ImageView) bookings.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_bolt);
        ((TextView) bookings.findViewById(R.id.tvStatLabel)).setText("Lượt đặt");
        ((TextView) bookings.findViewById(R.id.tvStatValue)).setText(String.valueOf(stats.getTotalBookings()));
        ((TextView) bookings.findViewById(R.id.tvTrendValue)).setText("Tổng số");

        // Audit Alert
        ((TextView) findViewById(R.id.tvAuditTitle)).setText(stats.getPendingReviews() + " báo cáo đang chờ");
        ((TextView) findViewById(R.id.tvAuditSubtitle)).setText("Cần được kiểm duyệt sớm");

        // Platform Health
        setupHealthItem(findViewById(R.id.healthConversations), "Cuộc trò chuyện", String.valueOf(stats.getTotalConversations()), "Realtime");
        setupHealthItem(findViewById(R.id.healthMessages), "Tin nhắn hệ thống", String.valueOf(stats.getTotalMessages()), "Live");
        setupHealthItem(findViewById(R.id.healthFields), "Sân đang hoạt động", stats.getAvailableFields() + "/" + stats.getTotalFields(), "Ok");
        setupHealthItem(findViewById(R.id.healthReviews), "Đánh giá cần duyệt", String.valueOf(stats.getPendingReviews()), "Alert");
    }

    private void setupHealthItem(View view, String label, String value, String trend) {
        ((TextView) view.findViewById(R.id.tvHealthLabel)).setText(label);
        ((TextView) view.findViewById(R.id.tvHealthValue)).setText(value);
        ((TextView) view.findViewById(R.id.tvHealthTrend)).setText(trend);
    }

    private void setupDefaultStats() {
        // Keep original fake data if API fails
        setupStats();
    }

    private void setupStats() {
        View gmv = findViewById(R.id.cardGmv);
        ((ImageView) gmv.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_wallet);
        ((TextView) gmv.findViewById(R.id.tvStatLabel)).setText("Doanh thu (GMV)");
        ((TextView) gmv.findViewById(R.id.tvStatValue)).setText("42.850.000đ");
        ((TextView) gmv.findViewById(R.id.tvTrendValue)).setText("+12.5%");

        View users = findViewById(R.id.cardUsers);
        ((ImageView) users.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_users);
        ((TextView) users.findViewById(R.id.tvStatLabel)).setText("Người dùng");
        ((TextView) users.findViewById(R.id.tvStatValue)).setText("1.248");
        ((TextView) users.findViewById(R.id.tvTrendValue)).setText("+8.3%");

        View fields = findViewById(R.id.cardFields);
        ((ImageView) fields.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_stadium);
        ((TextView) fields.findViewById(R.id.tvStatLabel)).setText("Sân bóng");
        ((TextView) fields.findViewById(R.id.tvStatValue)).setText("156");
        ((TextView) fields.findViewById(R.id.tvTrendValue)).setText("+2.1%");

        View bookings = findViewById(R.id.cardBookings);
        ((ImageView) bookings.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_bolt);
        ((TextView) bookings.findViewById(R.id.tvStatLabel)).setText("Lượt đặt");
        ((TextView) bookings.findViewById(R.id.tvStatValue)).setText("3.420");
        ((TextView) bookings.findViewById(R.id.tvTrendValue)).setText("+5.7%");
    }

    private void setupHealth() {
        setupHealthItem(findViewById(R.id.healthConversations), "Cuộc trò chuyện", "0", "Realtime");
        setupHealthItem(findViewById(R.id.healthMessages), "Tin nhắn hệ thống", "0", "Live");
        setupHealthItem(findViewById(R.id.healthFields), "Sân đang hoạt động", "0/0", "Ok");
        setupHealthItem(findViewById(R.id.healthReviews), "Đánh giá cần duyệt", "0", "Alert");
    }
}