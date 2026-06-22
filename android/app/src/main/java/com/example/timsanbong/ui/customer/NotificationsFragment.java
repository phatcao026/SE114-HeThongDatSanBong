package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.AppNotification;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;
    private TextView tvErrorState;
    private android.widget.ProgressBar pbLoading;

    private NotificationAdapter adapter;
    private NotificationViewModel notificationViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_customer_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners(view);
        loadData();
    }

    private void initViews(View view) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmptyNotifications = view.findViewById(R.id.tvEmptyNotifications);
        tvErrorState = view.findViewById(R.id.tvErrorState);
        pbLoading = view.findViewById(R.id.pbLoading);

        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            if (!notification.isRead()) {
                notificationViewModel.markAsRead(notification.getId());
            }
            String type = notification.getType();
            if (type != null && getActivity() instanceof CustomerMainActivity) {
                CustomerMainActivity mainActivity = (CustomerMainActivity) getActivity();
                switch (type.toUpperCase(java.util.Locale.US)) {
                    case "BOOKING_UPDATE":
                    case "PAYMENT_UPDATE":
                        mainActivity.switchToTab(NavBarManager.ITEM_BOOKINGS);
                        break;
                    case "MATCH_REQUEST":
                        mainActivity.switchToTab(NavBarManager.ITEM_MATCHMAKING);
                        break;
                    case "NEW_MESSAGE":
                        android.content.Intent chatIntent = new android.content.Intent(requireContext(), MessagesActivity.class);
                        startActivity(chatIntent);
                        break;
                    default:
                        break;
                }
            }
        });
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.ivNotifBack).setOnClickListener(v -> requireActivity().finish());
        view.findViewById(R.id.ivMarkAllRead).setOnClickListener(v ->
                notificationViewModel.markAllAsRead());
    }

    private void loadData() {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        notificationViewModel.notificationsState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.LOADING) {
                showLoading();
            } else if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                showNotifications(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                showError(resource.message);
            }
        });

        notificationViewModel.markReadState.observe(getViewLifecycleOwner(), resource -> {
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
