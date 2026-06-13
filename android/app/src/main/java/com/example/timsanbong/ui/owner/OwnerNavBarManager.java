package com.example.timsanbong.ui.owner;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.timsanbong.R;

public class OwnerNavBarManager {
    public static final int ITEM_DASHBOARD = 0;
    public static final int ITEM_FIELDS = 1;
    public static final int ITEM_BOOKINGS = 2;

    private final Activity activity;
    private final int activeItem;

    public OwnerNavBarManager(Activity activity, int activeItem) {
        this.activity = activity;
        this.activeItem = activeItem;
    }

    public void setup() {
        LinearLayout navDashboard = activity.findViewById(R.id.ownerNavDashboard);
        LinearLayout navFields = activity.findViewById(R.id.ownerNavFields);
        LinearLayout navBookings = activity.findViewById(R.id.ownerNavBookings);

        if (navDashboard == null || navFields == null || navBookings == null) {
            return;
        }

        applyActiveState(navDashboard, R.id.ivOwnerNavDashboard, R.id.tvOwnerNavDashboard,
                R.id.wrapOwnerNavDashboard, activeItem == ITEM_DASHBOARD);
        applyActiveState(navFields, R.id.ivOwnerNavFields, R.id.tvOwnerNavFields,
                R.id.wrapOwnerNavFields, activeItem == ITEM_FIELDS);
        applyActiveState(navBookings, R.id.ivOwnerNavBookings, R.id.tvOwnerNavBookings,
                R.id.wrapOwnerNavBookings, activeItem == ITEM_BOOKINGS);

        navDashboard.setOnClickListener(v -> {
            if (activeItem != ITEM_DASHBOARD) {
                navigate(OwnerDashboardActivity.class);
            }
        });
        navFields.setOnClickListener(v -> {
            if (activeItem != ITEM_FIELDS) {
                navigate(OwnerFieldManagementActivity.class);
            }
        });
        navBookings.setOnClickListener(v -> {
            if (activeItem != ITEM_BOOKINGS) {
                navigate(OwnerBookingActivity.class);
            }
        });
    }

    private void applyActiveState(LinearLayout container, int imageId, int textId, int iconWrapId,
                                  boolean active) {
        ImageView icon = container.findViewById(imageId);
        TextView label = container.findViewById(textId);
        FrameLayout iconWrap = container.findViewById(iconWrapId);
        int iconColor = ContextCompat.getColor(activity, active ? R.color.white : R.color.text_secondary);
        int textColor = ContextCompat.getColor(activity, active ? R.color.green_primary : R.color.text_secondary);

        if (icon != null) {
            icon.setColorFilter(iconColor);
        }
        if (label != null) {
            label.setTextColor(textColor);
            label.setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
        if (iconWrap != null) {
            iconWrap.setSelected(active);
        }
        container.setSelected(active);
    }

    private void navigate(Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
