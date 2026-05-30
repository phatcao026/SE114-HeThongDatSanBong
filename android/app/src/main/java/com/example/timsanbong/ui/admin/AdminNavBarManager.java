package com.example.timsanbong.ui.admin;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        applyActiveState(navHome, R.id.ivNavHome, R.id.tvNavHome, activeItem == ITEM_OVERVIEW);
        applyActiveState(navUsers, R.id.ivNavUsers, R.id.tvNavUsers, activeItem == ITEM_USERS);
        applyActiveState(navAudit, R.id.ivNavAudit, R.id.tvNavAudit, activeItem == ITEM_AUDIT);
        applyActiveState(navTransactions, R.id.ivNavTransactions, R.id.tvNavTransactions, activeItem == ITEM_TRANSACTIONS);
        applyActiveState(navProfile, R.id.ivNavProfile, R.id.tvNavProfile, activeItem == ITEM_PROFILE);

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

    private void applyActiveState(LinearLayout container, int ivId, int tvId, boolean active) {
        if (active) {
            container.setBackgroundResource(R.drawable.bg_nav_item_active);
            container.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(activity, android.R.color.white)));
            
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
            params.weight = 1.2f;
            params.setMargins(8, 8, 8, 8);
            container.setLayoutParams(params);
            container.setPadding(24, 0, 24, 0);
            container.setOrientation(LinearLayout.HORIZONTAL);

            ImageView iv = container.findViewById(ivId);
            TextView tv = container.findViewById(tvId);
            
            iv.setColorFilter(androidx.core.content.ContextCompat.getColor(activity, R.color.admin_nav_bg));
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.admin_nav_bg));
            tv.setTextSize(10);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Adjust margins for horizontal layout
            LinearLayout.LayoutParams tvParams = (LinearLayout.LayoutParams) tv.getLayoutParams();
            tvParams.setMarginStart(8);
            tv.setLayoutParams(tvParams);

            // Ensure the container itself is visible and has correct weight
            container.setVisibility(View.VISIBLE);
        } else {
            container.setBackground(null);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
            params.weight = 1.0f;
            params.setMargins(0, 0, 0, 0);
            container.setLayoutParams(params);
            container.setOrientation(LinearLayout.VERTICAL);

            ImageView iv = container.findViewById(ivId);
            TextView tv = container.findViewById(tvId);
            
            iv.setColorFilter(androidx.core.content.ContextCompat.getColor(activity, R.color.admin_nav_unselected));
            tv.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.admin_nav_unselected));
            tv.setTextSize(9);
            tv.setTypeface(null, android.graphics.Typeface.NORMAL);

            LinearLayout.LayoutParams tvParams = (LinearLayout.LayoutParams) tv.getLayoutParams();
            tvParams.setMarginStart(0);
            tv.setLayoutParams(tvParams);
        }
    }

    private void navigate(Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }
}