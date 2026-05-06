# 04 · Animation & Interaction
> Requires: `00_core.md`

---

## Timing Standards

| Action | Duration | Interpolator |
|--------|----------|-------------|
| Button press scale | 200ms | `fast_out_slow_in` |
| View enter | 250ms | `fast_out_slow_in` |
| View exit | 200ms | `fast_out_linear_in` |
| FAB appear/hide | 200ms | `fast_out_slow_in` |
| Bounce loop | 800ms | `BounceInterpolator` |

**Rule:** Always declare `android:interpolator` on every `<objectAnimator>`. Never omit it.

---

## Scroll Indicator (Bounce)

```xml
<ImageView
    android:id="@+id/iv_scroll_indicator"
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:layout_gravity="bottom|center_horizontal"
    android:layout_marginBottom="@dimen/spacing_16"
    android:src="@drawable/ic_keyboard_arrow_down"
    android:imageTintList="@color/text_hint"
    android:contentDescription="@null" />
```

```java
ObjectAnimator bounce = ObjectAnimator.ofFloat(ivScrollIndicator, "translationY", 0f, 16f);
bounce.setDuration(800);
bounce.setRepeatCount(ObjectAnimator.INFINITE);
bounce.setRepeatMode(ObjectAnimator.REVERSE);
bounce.setInterpolator(new BounceInterpolator());

// Respect system animation setting before starting
float scale = Settings.Global.getFloat(getContentResolver(),
        Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
if (scale > 0f) bounce.start();

// Hide on first scroll
recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
    @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        if (dy > 0 && ivScrollIndicator.getVisibility() == View.VISIBLE) {
            ivScrollIndicator.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> ivScrollIndicator.setVisibility(View.GONE)).start();
            bounce.cancel();
        }
    }
});
```

---

## Scroll-to-Top FAB

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab_scroll_top"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|end"
    android:layout_margin="@dimen/spacing_16"
    android:src="@drawable/ic_keyboard_arrow_up"
    android:contentDescription="@string/cd_scroll_to_top"
    app:backgroundTint="@color/primary"
    app:tint="@color/text_on_primary"
    android:visibility="gone" />
```

```java
FloatingActionButton fabScrollTop = findViewById(R.id.fab_scroll_top);
fabScrollTop.setAlpha(0f);

recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
    int scrolledDistance = 0;
    final int THRESHOLD = 300;

    @Override
    public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        scrolledDistance += dy;
        if (scrolledDistance > THRESHOLD && fabScrollTop.getVisibility() == View.GONE) {
            fabScrollTop.setVisibility(View.VISIBLE);
            fabScrollTop.animate().alpha(1f).setDuration(200)
                .setInterpolator(new FastOutSlowInInterpolator()).start();
        } else if (scrolledDistance <= THRESHOLD && fabScrollTop.getVisibility() == View.VISIBLE) {
            fabScrollTop.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> fabScrollTop.setVisibility(View.GONE)).start();
        }
    }
});

fabScrollTop.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
```

---

## Reduce Motion

Always check before starting decorative (non-essential) animations:

```java
float animatorScale = Settings.Global.getFloat(
    getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
if (animatorScale > 0f) {
    // start decorative animation
}
```

Transition animations (enter/exit) driven by Android framework automatically respect this setting.
