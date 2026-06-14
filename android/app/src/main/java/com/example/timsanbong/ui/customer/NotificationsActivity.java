package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.AppNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;

    private NotificationAdapter adapter;
    private NotificationViewModel notificationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_notifications);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyNotifications = findViewById(R.id.tvEmptyNotifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        findViewById(R.id.ivNotifBack).setOnClickListener(v -> finish());
        findViewById(R.id.ivMarkAllRead).setOnClickListener(v ->
                notificationViewModel.markAllAsRead());
    }

    private void loadData() {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        notificationViewModel.notificationsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                showNotifications(resource.data);
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                showNotifications(new ArrayList<>());
            }
        });

        notificationViewModel.loadNotifications(0, 50);
    }

    private void showNotifications(List<AppNotification> notifications) {
        adapter.updateNotifications(notifications);
        tvEmptyNotifications.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(notifications.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
