package com.example.timsanbong.ui.owner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.timsanbong.R;
import com.example.timsanbong.ui.customer.NotificationAdapter; // Bạn có thể dùng chung Adapter
import com.example.timsanbong.ui.customer.NotificationViewModel; // Bạn có thể dùng chung ViewModel
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class OwnerNotificationActivity extends AppCompatActivity {
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private NotificationViewModel viewModel;

    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_owner_notifications); // Bạn cần tạo layout này
        rvNotifications = findViewById(R.id.rvNotifications);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupRecyclerView();
        observeData();
        viewModel.loadNotifications(); // Gọi để tải danh sách thông báo
        }

        private void setupRecyclerView() {
            adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
                        // Xử lý khi click vào thông báo (ví dụ: chuyển sang màn hình booking)
                        viewModel.markNotificationRead(notification.getId());
                    });
             rvNotifications.setLayoutManager(new LinearLayoutManager(this));
               rvNotifications.setAdapter(adapter);
            }
        private void observeData() {
           viewModel.notificationsState.observe(this, resource -> {
                    if (resource != null && resource.data != null) {
                            adapter.updateNotifications(resource.data);
                    }
           });
        }
}
