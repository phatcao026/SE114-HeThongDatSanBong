package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.AppNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────
    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;

    // ── Data ─────────────────────────────────────────────
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.ivNotifBack).setOnClickListener(v -> finish());
        findViewById(R.id.ivMarkAllRead).setOnClickListener(v -> {
            // Mark all read — no-op on mock data
        });
    }

    private void loadData() {
        List<AppNotification> list = buildMockNotifications();
        notificationAdapter.updateNotifications(list);
        tvEmptyNotifications.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private List<AppNotification> buildMockNotifications() {
        List<AppNotification> list = new ArrayList<>();
        list.add(new AppNotification(R.drawable.ic_check, R.color.badge_green_text,
                "Bão Đông FC đã chấp nhận kèo của bạn",
                "Bắt đầu trận đấu lúc 18:00 tại Sân Thái Mỹ - Q1",
                "2 phút trước"));
        list.add(new AppNotification(R.drawable.ic_calendar, R.color.primary_dark,
                "Nhắc lịch: Sân Trần Bình - Bình Thạnh",
                "Lịch đặt sân của bạn vào CN - 15/06 lúc 06:00 sắp đến",
                "1 giờ trước"));
        list.add(new AppNotification(R.drawable.ic_bell, R.color.text_secondary,
                "Tin nhắn mới từ Phạm Quốc Khánh",
                "Ok mình sẽ tới sớm hơn",
                "2 giờ trước"));
        list.add(new AppNotification(R.drawable.ic_star, R.color.trust_mid,
                "Đánh giá uy tín của bạn tăng lên",
                "Điểm uy tín của bạn đã tăng lên 88 sau trận đấu hôm qua",
                "1 ngày trước"));
        return list;
    }
}
