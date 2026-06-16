package com.example.timsanbong.ui.admin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.timsanbong.R;

public class AdminNavBarManager {

    public static final int ITEM_OVERVIEW = 0;
    public static final int ITEM_USERS = 1;
    public static final int ITEM_AUDIT = 2;
    public static final int ITEM_TRANSACTIONS = 3;
    public static final int ITEM_PROFILE = 4;

    private final Activity activity;
    private final int activeItem;

    public AdminNavBarManager(Activity activity, int activeItem) {
        this.activity = activity;
        this.activeItem = activeItem;
    }

    public void setup() {
        LinearLayout navHome = activity.findViewById(R.id.adminNavHome);
        LinearLayout navUsers = activity.findViewById(R.id.adminNavUsers);
        LinearLayout navAudit = activity.findViewById(R.id.adminNavAudit);
        LinearLayout navTransactions = activity.findViewById(R.id.adminNavTransactions);
        LinearLayout navProfile = activity.findViewById(R.id.adminNavProfile);

        applyActiveState(navHome, R.id.wrapNavHome, R.id.ivNavHome, R.id.tvNavHome, activeItem == ITEM_OVERVIEW);
        applyActiveState(navUsers, R.id.wrapNavUsers, R.id.ivNavUsers, R.id.tvNavUsers, activeItem == ITEM_USERS);
        applyActiveState(navAudit, R.id.wrapNavAudit, R.id.ivNavAudit, R.id.tvNavAudit, activeItem == ITEM_AUDIT);
        applyActiveState(navTransactions, R.id.wrapNavTransactions, R.id.ivNavTransactions, R.id.tvNavTransactions, activeItem == ITEM_TRANSACTIONS);
        applyActiveState(navProfile, R.id.wrapNavProfile, R.id.ivNavProfile, R.id.tvNavProfile, activeItem == ITEM_PROFILE);

        navHome.setOnClickListener(v -> {
            if (activeItem != ITEM_OVERVIEW) navigate(AdminMainActivity.class);
        });

        navUsers.setOnClickListener(v -> {
            if (activeItem != ITEM_USERS) navigate(AdminUserActivity.class);
        });

        navAudit.setOnClickListener(v -> {
            if (activeItem != ITEM_AUDIT) navigate(AdminAuditActivity.class);
        });

        navTransactions.setOnClickListener(v -> {
            if (activeItem != ITEM_TRANSACTIONS) navigate(AdminTransactionActivity.class);
        });
        
        navProfile.setOnClickListener(v -> {
            if (activeItem != ITEM_PROFILE) navigate(AdminProfileActivity.class);
        });
    }

    private void applyActiveState(View container, int wrapId, int ivId, int tvId, boolean active) {
        int iconColor = ContextCompat.getColor(
            activity, active ? R.color.primary_dark : R.color.text_on_primary);

        View wrap = container.findViewById(wrapId);
        wrap.setBackground(active ? ContextCompat.getDrawable(activity, R.drawable.bg_nav_active_pill) : null);

        ImageViewCompat.setImageTintList(
            container.findViewById(ivId),
            ColorStateList.valueOf(iconColor));

        TextView label = container.findViewById(tvId);
        label.setTextColor(ContextCompat.getColor(activity, R.color.text_on_primary));
    }

    private void navigate(Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }
}