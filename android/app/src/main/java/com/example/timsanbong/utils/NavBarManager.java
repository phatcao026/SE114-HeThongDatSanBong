package com.example.timsanbong.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.customer.BookingActivity;
import com.example.timsanbong.ui.customer.MyBookingsActivity;
import com.example.timsanbong.ui.profile.ProfileActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class NavBarManager {

    public static final int ITEM_HOME = 0;
    public static final int ITEM_EXPLORE = 1;
    public static final int ITEM_BOOKINGS = 2;
    public static final int ITEM_PROFILE = 3;

    private final Activity activity;
    private final int activeItem;

    public NavBarManager(Activity activity, int activeItem) {
        this.activity = activity;
        this.activeItem = activeItem;
    }

    public void setup() {
        LinearLayout navContainer = activity.findViewById(R.id.navContainer);
        LinearLayout navHome = activity.findViewById(R.id.navHome);
        LinearLayout navExplore = activity.findViewById(R.id.navExplore);
        LinearLayout navBookings = activity.findViewById(R.id.navBookings);
        LinearLayout navProfile = activity.findViewById(R.id.navProfile);
        FloatingActionButton fabQuickBook = activity.findViewById(R.id.fabQuickBook);

        applyNavBarShape(navContainer);

        // Ensure FAB appears on top of nav bar by setting translation Z
        float fabElevation = activity.getResources().getDimension(R.dimen.shadow_lg);
        fabQuickBook.setTranslationZ(fabElevation * 2);

        applyActiveState(navHome, R.id.ivNavHome, R.id.dotNavHome, activeItem == ITEM_HOME);
        applyActiveState(navExplore, R.id.ivNavExplore, R.id.dotNavExplore, activeItem == ITEM_EXPLORE);
        applyActiveState(navBookings, R.id.ivNavBookings, R.id.dotNavBookings, activeItem == ITEM_BOOKINGS);
        applyActiveState(navProfile, R.id.ivNavProfile, R.id.dotNavProfile, activeItem == ITEM_PROFILE);

        navHome.setOnClickListener(v -> {
            if (activeItem != ITEM_HOME) navigate(MainActivity.class);
        });
        navExplore.setOnClickListener(v -> {
            if (activeItem != ITEM_EXPLORE) navigate(MainActivity.class);
        });
        navBookings.setOnClickListener(v -> {
            if (activeItem != ITEM_BOOKINGS) navigate(MyBookingsActivity.class);
        });
        navProfile.setOnClickListener(v -> {
            if (activeItem != ITEM_PROFILE) navigate(ProfileActivity.class);
        });

        fabQuickBook.setOnClickListener(v ->
            activity.startActivity(new Intent(activity, BookingActivity.class)));

        animateFabIn(fabQuickBook);
    }

    /**
     * Bar shape: pill with a concave circular notch on the top-center edge.
     * The notch is drawn by MaterialShapeDrawable (visual); the OutlineProvider stays convex
     * so the system draws a correct pill shadow rather than clipping into the notch.
     * Z-order: navContainer elevation = shadow_lg; FAB translationZ = shadow_lg on top of
     * FAB natural elevation (~6dp), so FAB total Z ≈ 22dp > 16dp → FAB always on top. ✓
     */
    private void applyNavBarShape(LinearLayout navContainer) {
        android.util.DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        float fabRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, dm);
        float cradleMargin = activity.getResources().getDimension(R.dimen.spacing_8);
        float cradleRadius = fabRadius + cradleMargin;
        float cornerRadius = activity.getResources().getDimension(R.dimen.radius_nav);
        float strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, dm);

        ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder()
            .setTopEdge(new CircularEdgeTreatment(cradleRadius, true))
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .build();

        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeModel);
        shapeDrawable.setFillColor(ColorStateList.valueOf(
            ContextCompat.getColor(activity, R.color.background_secondary)));
        shapeDrawable.setStroke(strokeWidthPx,
            ContextCompat.getColor(activity, R.color.primary));

        ViewCompat.setBackground(navContainer, shapeDrawable);

        // Elevation with a convex pill outline so Android draws the correct shadow.
        // The visual notch is handled by shapeDrawable's fill; the shadow stays pill-shaped.
        float elevation = activity.getResources().getDimension(R.dimen.shadow_lg);
        ViewCompat.setElevation(navContainer, elevation);
        navContainer.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
    }

    private void applyActiveState(LinearLayout container, int ivId, int dotId, boolean active) {
        int color = ContextCompat.getColor(
            activity, active ? R.color.primary_dark : R.color.text_hint);

        ImageViewCompat.setImageTintList(
            container.findViewById(ivId),
            ColorStateList.valueOf(color));

        container.findViewById(dotId).setVisibility(active ? View.VISIBLE : View.GONE);
    }

    private void navigate(Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    private void animateFabIn(FloatingActionButton fab) {
        float animScale = Settings.Global.getFloat(
            activity.getContentResolver(),
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        if (animScale > 0f) {
            fab.setScaleX(0f);
            fab.setScaleY(0f);
            fab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
        }
    }
}
