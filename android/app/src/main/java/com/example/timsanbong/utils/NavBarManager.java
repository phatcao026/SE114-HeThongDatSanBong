package com.example.timsanbong.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.customer.FindPitchActivity;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.customer.MatchmakingActivity;
import com.example.timsanbong.ui.customer.MessagesActivity;
import com.example.timsanbong.ui.customer.MyBookingsActivity;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class NavBarManager {

    public static final int ITEM_HOME = 0;
    public static final int ITEM_SEARCH = 1;
    public static final int ITEM_MATCH = 2;
    public static final int ITEM_BOOKINGS = 3;
    public static final int ITEM_MESSAGES = 4;

    private final Activity activity;
    private final int activeItem;

    public NavBarManager(Activity activity, int activeItem) {
        this.activity = activity;
        this.activeItem = activeItem;
    }

    public void setup() {
        LinearLayout navContainer = activity.findViewById(R.id.navContainer);
        LinearLayout navHome = activity.findViewById(R.id.navHome);
        LinearLayout navSearch = activity.findViewById(R.id.navSearch);
        LinearLayout navMatch = activity.findViewById(R.id.navMatch);
        LinearLayout navBookings = activity.findViewById(R.id.navBookings);
        LinearLayout navMessages = activity.findViewById(R.id.navMessages);

        applyNavBarShape(navContainer);

        applyActiveState(navHome, R.id.wrapNavHome, R.id.ivNavHome, R.id.tvNavHome, activeItem == ITEM_HOME);
        applyActiveState(navSearch, R.id.wrapNavSearch, R.id.ivNavSearch, R.id.tvNavSearch, activeItem == ITEM_SEARCH);
        applyActiveState(navMatch, R.id.wrapNavMatch, R.id.ivNavMatch, R.id.tvNavMatch, activeItem == ITEM_MATCH);
        applyActiveState(navBookings, R.id.wrapNavBookings, R.id.ivNavBookings, R.id.tvNavBookings, activeItem == ITEM_BOOKINGS);
        applyActiveState(navMessages, R.id.wrapNavMessages, R.id.ivNavMessages, R.id.tvNavMessages, activeItem == ITEM_MESSAGES);

        navHome.setOnClickListener(v -> {
            if (activeItem != ITEM_HOME) navigate(MainActivity.class);
        });
        navSearch.setOnClickListener(v -> {
            if (activeItem != ITEM_SEARCH) navigate(FindPitchActivity.class);
        });
        navMatch.setOnClickListener(v -> {
            if (activeItem != ITEM_MATCH) navigate(MatchmakingActivity.class);
        });
        navBookings.setOnClickListener(v -> {
            if (activeItem != ITEM_BOOKINGS) navigate(MyBookingsActivity.class);
        });
        navMessages.setOnClickListener(v -> {
            if (activeItem != ITEM_MESSAGES) navigate(MessagesActivity.class);
        });
    }

    /**
     * Bar shape: pill with rounded corners. Outline stays convex for correct shadow.
     */
    private void applyNavBarShape(LinearLayout navContainer) {
        android.util.DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        float cornerRadius = 0f;
        float strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, dm);

        ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .build();

        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeModel);
        shapeDrawable.setFillColor(ColorStateList.valueOf(
            ContextCompat.getColor(activity, R.color.primary)));
        shapeDrawable.setStroke(strokeWidthPx,
            ContextCompat.getColor(activity, R.color.primary));

        ViewCompat.setBackground(navContainer, shapeDrawable);
        ViewCompat.setElevation(navContainer, 0f);
    }

    private void applyActiveState(LinearLayout container, int wrapId, int ivId, int tvId, boolean active) {
        int iconColor = ContextCompat.getColor(
            activity, active ? R.color.primary_dark : R.color.text_on_primary);

        View wrap = container.findViewById(wrapId);
        wrap.setBackground(active ? ContextCompat.getDrawable(activity, R.drawable.bg_nav_active_pill) : null);

        ImageViewCompat.setImageTintList(
            container.findViewById(ivId),
            ColorStateList.valueOf(iconColor));

        android.widget.TextView label = container.findViewById(tvId);
        label.setTextColor(ContextCompat.getColor(activity, R.color.text_on_primary));
    }

    private void navigate(Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

}
