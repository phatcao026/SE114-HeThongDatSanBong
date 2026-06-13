package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.auth.LoginActivity;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.owner.OwnerDashboardActivity;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

public class AdminProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        setupMenus();

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_PROFILE);
        navBarManager.setup();
    }

    private void setupMenus() {
        // Role switching
        setupMenuItem(findViewById(R.id.menuPlayer), R.drawable.ic_person, "Người chơi", "#F0FDF4", "#166534", false, v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        setupMenuItem(findViewById(R.id.menuOwner), R.drawable.ic_admin_stadium, "Chủ sân", "#ECFDF5", "#059669", false, v -> {
            Intent intent = new Intent(this, OwnerDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        setupMenuItem(findViewById(R.id.menuAdmin), R.drawable.ic_admin_shield, "Quản trị viên", "#166534", "#FFFFFF", true, null);

        // System
        setupMenuItem(findViewById(R.id.menuPerms), R.drawable.ic_admin_users, "Phân quyền & nhân sự", "transparent", "#64748B", false, null);
        setupMenuItem(findViewById(R.id.menuLogs), R.drawable.ic_message, "Nhật ký kiểm duyệt", "transparent", "#64748B", false, null);
        setupMenuItem(findViewById(R.id.menuConfig), R.drawable.ic_admin_dashboard, "Cấu hình nền tảng", "transparent", "#64748B", false, null);
        setupMenuItem(findViewById(R.id.menuApi), R.drawable.ic_admin_key, "API & webhooks", "transparent", "#64748B", false, null);
        setupMenuItem(findViewById(R.id.menuLogout), R.drawable.ic_admin_logout, "Đăng xuất", "transparent", "#EF4444", false, v -> {
            new SessionManager(this).clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Styling for System icons (no background card)
        int[] systemIds = {R.id.menuPerms, R.id.menuLogs, R.id.menuConfig, R.id.menuApi, R.id.menuLogout};
        for (int id : systemIds) {
            MaterialCardView cv = findViewById(id).findViewById(R.id.cvMenuIcon);
            cv.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
            cv.setRadius(0);
        }

        // Specific styling for Logout
        View logout = findViewById(R.id.menuLogout);
        ((TextView) logout.findViewById(R.id.tvMenuLabel)).setTextColor(ContextCompat.getColor(this, R.color.error));
        logout.findViewById(R.id.ivMenuChevron).setVisibility(View.GONE);
    }

    private void setupMenuItem(View view, int iconRes, String label, String bgColor, String iconColor, boolean isCurrent, View.OnClickListener customListener) {
        ImageView ivIcon = view.findViewById(R.id.ivMenuIcon);
        TextView tvLabel = view.findViewById(R.id.tvMenuLabel);
        TextView tvBadge = view.findViewById(R.id.tvMenuBadge);
        MaterialCardView cvIcon = view.findViewById(R.id.cvMenuIcon);
        
        ivIcon.setImageResource(iconRes);
        ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(iconColor)));
        tvLabel.setText(label);
        
        if (!bgColor.equals("transparent")) {
            cvIcon.setCardBackgroundColor(android.graphics.Color.parseColor(bgColor));
        }
        
        if (isCurrent) {
            tvBadge.setVisibility(View.VISIBLE);
        } else {
            tvBadge.setVisibility(View.GONE);
        }

        if (customListener != null) {
            view.setOnClickListener(customListener);
        } else {
            view.setOnClickListener(v -> Toast.makeText(this, "Chuyển đến: " + label, Toast.LENGTH_SHORT).show());
        }
    }
}