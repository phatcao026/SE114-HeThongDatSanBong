package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminUserActivity extends AppCompatActivity {

    private AdminUserAdapter adapter;
    private List<AdminUser> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        RecyclerView rvUsers = findViewById(R.id.rvAdminUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        
        allUsers = new ArrayList<>();
        allUsers.add(new AdminUser("Trần Đăng Khoa", "ĐK", "92", "Người chơi · 0901 234 567 · 38 đặt sân", "Hoạt động", "#F0FDF4", "#60D86E", "Người chơi"));
        allUsers.add(new AdminUser("Đỗ Văn Sang", "VS", "28", "Người chơi · 0908 222 444 · 12 đặt sân", "Cảnh báo", "#FFFBEB", "#F59E0B", "Người chơi"));
        allUsers.add(new AdminUser("Hoàng Tấn Lực", "TL", "14", "Người chơi · 0902 555 111 · 8 đặt sân", "Đã khóa", "#FEF2F2", "#EF4444", "Đã khóa"));
        allUsers.add(new AdminUser("Sân Phú Mỹ Hưng", "MH", "65", "Chủ sân · 0905 678 901 · 0 đặt sân", "Chờ duyệt", "#EFF6FF", "#3B82F6", "Chủ sân"));
        allUsers.add(new AdminUser("Lê Văn Tám", "VT", "45", "Người chơi · 0903 111 222 · 20 đặt sân", "Hoạt động", "#F0FDF4", "#60D86E", "Người chơi"));
        allUsers.add(new AdminUser("Nguyễn Thị Mai", "TM", "12", "Người chơi · 0904 333 444 · 5 đặt sân", "Hoạt động", "#F0FDF4", "#60D86E", "Người chơi"));
        allUsers.add(new AdminUser("Trần Bình Trọng", "BT", "77", "Chủ sân · 0906 777 888 · 0 đặt sân", "Hoạt động", "#F0FDF4", "#60D86E", "Chủ sân"));
        allUsers.add(new AdminUser("Phạm Ngũ Lão", "NL", "99", "Người chơi · 0907 999 000 · 100 đặt sân", "Hoạt động", "#F0FDF4", "#60D86E", "Người chơi"));

        adapter = new AdminUserAdapter(new ArrayList<>(allUsers));
        rvUsers.setAdapter(adapter);
        rvUsers.setNestedScrollingEnabled(false);

        setupFilters();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_USERS);
        navBarManager.setup();
    }

    private void setupFilters() {
        MaterialButton btnAll = findViewById(R.id.btnFilterAll);
        MaterialButton btnPlayer = findViewById(R.id.btnFilterPlayer);
        MaterialButton btnOwner = findViewById(R.id.btnFilterOwner);
        MaterialButton btnLocked = findViewById(R.id.btnFilterLocked);

        btnAll.setOnClickListener(v -> {
            updateFilterButtons(btnAll, btnPlayer, btnOwner, btnLocked);
            filterUsers("Tất cả");
        });
        btnPlayer.setOnClickListener(v -> {
            updateFilterButtons(btnPlayer, btnAll, btnOwner, btnLocked);
            filterUsers("Người chơi");
        });
        btnOwner.setOnClickListener(v -> {
            updateFilterButtons(btnOwner, btnAll, btnPlayer, btnLocked);
            filterUsers("Chủ sân");
        });
        btnLocked.setOnClickListener(v -> {
            updateFilterButtons(btnLocked, btnAll, btnPlayer, btnOwner);
            filterUsers("Đã khóa");
        });
    }

    private void updateFilterButtons(MaterialButton selected, MaterialButton... others) {
        selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)));
        selected.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        selected.setStrokeWidth(0);

        String text = selected.getText().toString();
        if (!text.contains("✓")) {
            selected.setText("✓ " + text);
        }

        for (MaterialButton btn : others) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.admin_background)));
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_heading));
            btn.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
            btn.setStrokeColor(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_gray)));

            String otherText = btn.getText().toString();
            if (otherText.contains("✓")) {
                btn.setText(otherText.replace("✓ ", "").replace("✓", "").trim());
            }
        }
    }

    private void filterUsers(String role) {
        List<AdminUser> filteredList = new ArrayList<>();
        if (role.equals("Tất cả")) {
            filteredList.addAll(allUsers);
        } else {
            for (AdminUser user : allUsers) {
                if (user.role.equals(role)) {
                    filteredList.add(user);
                }
            }
        }
        adapter.updateList(filteredList);
        
        // Update count text
        TextView tvCount = findViewById(R.id.tvUserCount);
        tvCount.setText(filteredList.size() + " người dùng");
    }

    static class AdminUser {
        String name, initials, level, detail, status, statusBg, statusColor, role;
        AdminUser(String n, String i, String l, String d, String s, String sb, String sc, String r) {
            name = n; initials = i; level = l; detail = d; status = s; statusBg = sb; statusColor = sc; role = r;
        }
    }

    static class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
        private final List<AdminUser> users;
        AdminUserAdapter(List<AdminUser> users) { this.users = users; }

        public void updateList(List<AdminUser> newList) {
            users.clear();
            users.addAll(newList);
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AdminUser user = users.get(position);
            holder.tvName.setText(user.name);
            holder.tvInitials.setText(user.initials);
            holder.tvLevel.setText(user.level);
            holder.tvDetail.setText(user.detail);
            holder.tvStatus.setText("● " + user.status);
            
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(user.statusBg)));
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor(user.statusColor));
        }

        @Override
        public int getItemCount() { return users.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvInitials, tvLevel, tvDetail, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvUserName);
                tvInitials = v.findViewById(R.id.tvInitials);
                tvLevel = v.findViewById(R.id.tvUserLevel);
                tvDetail = v.findViewById(R.id.tvUserDetail);
                tvStatus = v.findViewById(R.id.tvStatusBadge);
            }
        }
    }
}