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
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;
    private TextView tvErrorState;
    private android.widget.ProgressBar pbLoading;

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
        tvErrorState = findViewById(R.id.tvErrorState);
        pbLoading = findViewById(R.id.pbLoading);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            if (!notification.isRead()) {
                notificationViewModel.markAsRead(notification.getId());
            }
        });
        rvNotifications.setAdapter(adapter);
        new NavBarManager(this, NavBarManager.ITEM_NOTIFICATIONS).setup();
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
            if (resource.status == Resource.Status.LOADING) {
                showLoading();
            } else if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                showNotifications(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                showError(resource.message);
            }
        });

        notificationViewModel.markReadState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS) {
                notificationViewModel.loadNotifications(0, 50);
                notificationViewModel.loadUnreadCount();
            }
        });

        notificationViewModel.loadNotifications(0, 50);
        notificationViewModel.loadUnreadCount();
    }

    private void showNotifications(List<AppNotification> notifications) {
        adapter.updateNotifications(notifications);
        pbLoading.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.GONE);
        tvEmptyNotifications.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(notifications.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading() {
        pbLoading.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.GONE);
        tvEmptyNotifications.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.GONE);
    }

    private void showError(String message) {
        pbLoading.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);
        tvEmptyNotifications.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.VISIBLE);
        tvErrorState.setText(message == null || message.trim().isEmpty()
                ? getString(R.string.error_unknown)
                : message);
    }
}
