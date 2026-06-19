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
import android.content.Intent;

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

        adapter.setOnItemClickListener(user -> {
            Intent intent = new Intent(this, AdminUserDetailActivity.class);
            intent.putExtra("userId", user.getId());
            intent.putExtra("fullName", user.getFullName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phone", user.getPhone());
            intent.putExtra("role", user.getRole());
            intent.putExtra("isLocked", user.isLocked());
            startActivityForResult(intent, 1001);
        });

        fetchUsers();
        setupFilters();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            fetchUsers();
            swipeRefresh.setRefreshing(false);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_USERS);
        navBarManager.setup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            fetchUsers();
        }
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
                Toast.makeText(AdminUserActivity.this, "Lỗi kết nối - Sử dụng dữ liệu demo", Toast.LENGTH_SHORT).show();
                setupDemoUsers();
            }
        });
    }

    private void setupDemoUsers() {
        allUsers = new ArrayList<>();
        allUsers.add(new User(101, "Nguyễn Văn A", "admin@test.com", "0901234567", "ADMIN"));
        allUsers.add(new User(102, "Trần Thị B", "owner@test.com", "0902345678", "OWNER"));
        allUsers.add(new User(103, "Lê Văn C", "player@test.com", "0903456789", "PLAYER"));
        allUsers.add(new User(104, "Phạm Minh D", "user4@test.com", "0904567890", "PLAYER"));
        allUsers.add(new User(105, "Hoàng Anh E", "user5@test.com", "0905678901", "PLAYER"));
        
        adapter.updateList(allUsers);
        TextView tvCount = findViewById(R.id.tvUserCount);
        tvCount.setText(allUsers.size() + " người dùng");
    }

    private void setupFilters() {
        MaterialButton btnAll = findViewById(R.id.btnFilterAll);
        MaterialButton btnPlayer = findViewById(R.id.btnFilterPlayer);
        MaterialButton btnOwner = findViewById(R.id.btnFilterOwner);
        MaterialButton btnLocked = findViewById(R.id.btnFilterLocked);
        MaterialButton btnLowTrust = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btnLowTrust.setText("Uy tín thấp");
        btnLowTrust.setAllCaps(false);
        ((ViewGroup) btnAll.getParent()).addView(btnLowTrust);

        btnAll.setOnClickListener(v -> {
            updateFilterButtons(btnAll, btnPlayer, btnOwner, btnLocked, btnLowTrust);
            filterUsers("Tất cả");
        });
        btnPlayer.setOnClickListener(v -> {
            updateFilterButtons(btnPlayer, btnAll, btnOwner, btnLocked, btnLowTrust);
            filterUsers("Người chơi");
        });
        btnOwner.setOnClickListener(v -> {
            updateFilterButtons(btnOwner, btnAll, btnPlayer, btnLocked, btnLowTrust);
            filterUsers("Chủ sân");
        });
        btnLocked.setOnClickListener(v -> {
            updateFilterButtons(btnLocked, btnAll, btnPlayer, btnOwner, btnLowTrust);
            filterUsers("Đã khóa");
        });
        btnLowTrust.setOnClickListener(v -> {
            updateFilterButtons(btnLowTrust, btnAll, btnPlayer, btnOwner, btnLocked);
            filterUsers("LowTrust");
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
        } else if (role.equals("Đã khóa")) {
            for (User user : allUsers) {
                if (user.isLocked()) {
                    filteredList.add(user);
                }
            }
        } else if (role.equals("LowTrust")) {
            for (User user : allUsers) {
                if (user.getTrustScore() < 80) {
                    filteredList.add(user);
                }
            }
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
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(User user);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

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
            holder.tvLevel.setText(String.valueOf(user.getTrustScore()));
            holder.tvDetail.setText(user.getRole() + " · " + user.getPhone() + " · " + user.getEmail());
            
            if (user.isLocked()) {
                holder.tvStatus.setText("● Đã khóa");
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444")));
                holder.tvStatus.setTextColor(android.graphics.Color.WHITE);
            } else {
                holder.tvStatus.setText("● Hoạt động");
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0FDF4")));
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#60D86E"));
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(user);
            });
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