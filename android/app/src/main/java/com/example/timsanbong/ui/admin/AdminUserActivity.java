package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.User;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserActivity extends AppCompatActivity {

    private AdminUserAdapter adapter;
    private List<User> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        RecyclerView rvUsers = findViewById(R.id.rvAdminUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        
        allUsers = new ArrayList<>();
        adapter = new AdminUserAdapter(new ArrayList<>());
        rvUsers.setAdapter(adapter);
        rvUsers.setNestedScrollingEnabled(false);

        fetchUsers();
        setupFilters();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_USERS);
        navBarManager.setup();
    }

    private void fetchUsers() {
        ApiClient.getService(this).getAdminUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();
                    adapter.updateList(allUsers);
                    TextView tvCount = findViewById(R.id.tvUserCount);
                    tvCount.setText(allUsers.size() + " người dùng");
                } else {
                    Toast.makeText(AdminUserActivity.this, "Không thể tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminUserActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
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
        List<User> filteredList = new ArrayList<>();
        if (role.equals("Tất cả")) {
            filteredList.addAll(allUsers);
        } else {
            String roleKey = role.equals("Người chơi") ? "PLAYER" : (role.equals("Chủ sân") ? "OWNER" : role);
            for (User user : allUsers) {
                if (user.getRole() != null && user.getRole().equalsIgnoreCase(roleKey)) {
                    filteredList.add(user);
                }
            }
        }
        adapter.updateList(filteredList);
        
        // Update count text
        TextView tvCount = findViewById(R.id.tvUserCount);
        tvCount.setText(filteredList.size() + " người dùng");
    }

    static class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
        private final List<User> users;
        AdminUserAdapter(List<User> users) { this.users = users; }

        public void updateList(List<User> newList) {
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
            User user = users.get(position);
            holder.tvName.setText(user.getFullName());
            String initials = user.getFullName() != null && user.getFullName().length() >= 2 ? 
                    user.getFullName().substring(0, 2).toUpperCase() : "U";
            holder.tvInitials.setText(initials);
            holder.tvLevel.setText("100"); // Trust score placeholder
            holder.tvDetail.setText(user.getRole() + " · " + user.getPhone() + " · " + user.getEmail());
            holder.tvStatus.setText("● Hoạt động");
            
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0FDF4")));
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#60D86E"));
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

    @Deprecated
    static class AdminUser {
        String name, initials, level, detail, status, statusBg, statusColor, role;
        AdminUser(String n, String i, String l, String d, String s, String sb, String sc, String r) {
            name = n; initials = i; level = l; detail = d; status = s; statusBg = sb; statusColor = sc; role = r;
        }
    }
}