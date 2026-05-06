# 03 · Navigation & Layout
> Requires: `00_core.md`

---

## Floating Pill Navigation Bar

```xml
<!-- res/layout/layout_floating_nav.xml -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|center_horizontal"
    android:layout_marginBottom="@dimen/spacing_24"
    app:cardCornerRadius="@dimen/radius_nav"
    app:cardElevation="@dimen/shadow_lg"
    app:cardBackgroundColor="@color/background_secondary"
    app:strokeWidth="1dp"
    app:strokeColor="@color/border_gray">

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_nav"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:itemIconTint="@color/nav_item_color_state"
        app:itemTextColor="@color/nav_item_color_state"
        app:itemBackground="@color/nav_item_bg_state"
        app:labelVisibilityMode="labeled"
        app:menu="@menu/menu_bottom_nav" />

</com.google.android.material.card.MaterialCardView>
```

```xml
<!-- res/color/nav_item_color_state.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_checked="true" android:color="@color/primary" />
    <item                              android:color="@color/text_hint" />
</selector>

<!-- res/color/nav_item_bg_state.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_checked="true" android:color="@color/primary_light" />
    <item                              android:color="@android:color/transparent" />
</selector>
```

Note: Blur effect (`app:cardBackgroundColor="#F2FFFFFF"` + `RenderEffect`) requires API 31+. Default to solid `background_secondary` for API < 31.

---

## Notification Badge on Nav Icon

```java
// Show
BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_notifications);
badge.setNumber(5);
badge.setBackgroundColor(getColor(R.color.color_error));
badge.setBadgeTextColor(getColor(R.color.background_primary));

// Hide
bottomNav.removeBadge(R.id.nav_notifications);
```

---

## Layout Patterns

### Full-screen section
```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_primary" />
```

### Vertical centering — LinearLayout
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center_vertical" />
```

### Vertical centering — ConstraintLayout
```xml
<View
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent" />
```

---

## Responsive / Foldable (API 30+)

```java
WindowMetrics windowMetrics = WindowMetricsCalculator.getOrCreate()
        .computeCurrentWindowMetrics(this);
float dpWidth = windowMetrics.getBounds().width()
        / getResources().getDisplayMetrics().density;

if (dpWidth >= 840) {
    // Large screen: two-pane layout
} else {
    // Compact: single-pane
}
```

Provide `res/layout/` (compact) as default. Note in comments when `res/layout-sw840dp/` is needed.
