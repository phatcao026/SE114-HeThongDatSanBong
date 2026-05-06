# 01 · Components
> Requires: `00_core.md`

---

## Button — Primary

```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:minHeight="48dp"
    android:fontFamily="@font/inter"
    android:textSize="14sp"
    android:textStyle="bold"
    android:textColor="@color/btn_primary_text_color"
    android:paddingHorizontal="@dimen/spacing_20"
    android:paddingVertical="@dimen/spacing_12"
    app:cornerRadius="@dimen/radius_pill"
    app:backgroundTint="@color/btn_primary_bg_tint"
    app:rippleColor="@color/primary_dark"
    android:stateListAnimator="@animator/button_scale_animator" />
```

```xml
<!-- res/color/btn_primary_bg_tint.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_enabled="false" android:color="@color/border_gray" />
    <item                               android:color="@color/primary" />
</selector>

<!-- res/color/btn_primary_text_color.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_enabled="false" android:color="@color/text_hint" />
    <item                               android:color="@color/text_on_primary" />
</selector>
```

```xml
<!-- res/animator/button_scale_animator.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <set>
            <objectAnimator android:propertyName="scaleX" android:duration="200"
                android:valueTo="0.95" android:valueType="floatType"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
            <objectAnimator android:propertyName="scaleY" android:duration="200"
                android:valueTo="0.95" android:valueType="floatType"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
        </set>
    </item>
    <item>
        <set>
            <objectAnimator android:propertyName="scaleX" android:duration="200"
                android:valueTo="1.0" android:valueType="floatType"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
            <objectAnimator android:propertyName="scaleY" android:duration="200"
                android:valueTo="1.0" android:valueType="floatType"
                android:interpolator="@android:interpolator/fast_out_slow_in"/>
        </set>
    </item>
</selector>
```

## Button — Outline

```xml
<com.google.android.material.button.MaterialButton
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"
    android:minHeight="48dp"
    android:fontFamily="@font/inter"
    android:textColor="@color/primary_text"
    android:paddingHorizontal="@dimen/spacing_20"
    android:paddingVertical="@dimen/spacing_12"
    app:cornerRadius="@dimen/radius_pill"
    app:strokeColor="@color/primary"
    app:strokeWidth="1.5dp"
    app:backgroundTint="@android:color/transparent"
    app:rippleColor="@color/primary_light"
    android:stateListAnimator="@animator/button_scale_animator" />
```

---

## Card

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="@dimen/spacing_16"
    android:layout_marginBottom="@dimen/spacing_12"
    app:cardBackgroundColor="@color/background_secondary"
    app:cardCornerRadius="@dimen/radius_card"
    app:cardElevation="@dimen/shadow_sm"
    app:strokeWidth="1dp"
    app:strokeColor="@color/border_gray"
    app:contentPadding="@dimen/spacing_24">

    <!-- Thumbnail placeholder -->
    <View
        android:layout_width="match_parent"
        android:layout_height="160dp"
        android:background="@color/background_tertiary" />

</com.google.android.material.card.MaterialCardView>
```

---

## EditText / TextInputLayout

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/hint_email"
    app:boxCornerRadiusTopStart="@dimen/radius_pill"
    app:boxCornerRadiusTopEnd="@dimen/radius_pill"
    app:boxCornerRadiusBottomStart="@dimen/radius_pill"
    app:boxCornerRadiusBottomEnd="@dimen/radius_pill"
    app:boxStrokeColor="@color/edittext_stroke_color"
    app:boxStrokeWidth="1dp"
    app:boxStrokeWidthFocused="2dp"
    app:hintTextColor="@color/primary"
    app:errorTextColor="@color/color_error"
    app:errorIconTint="@color/color_error">

    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="52dp"
        android:fontFamily="@font/inter"
        android:textSize="14sp"
        android:textColor="@color/text_heading"
        android:paddingHorizontal="@dimen/spacing_20" />

</com.google.android.material.textfield.TextInputLayout>
```

```xml
<!-- res/color/edittext_stroke_color.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_activated="true" android:color="@color/color_error" />
    <item android:state_focused="true"   android:color="@color/primary" />
    <item                                android:color="@color/border_gray" />
</selector>
```

Validation states (Java):
```java
layout.setError("Email không hợp lệ"); // show error
layout.setError(null);                  // clear error
```

| State | Stroke | Width |
|-------|--------|-------|
| Default | `border_gray` | 1dp |
| Focused | `primary` | 2dp |
| Error | `color_error` | 2dp |
| Disabled | `border_gray` + alpha 0.38 | 1dp |

---

## StatusBadge

```xml
<!-- res/layout/view_status_badge.xml -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:paddingHorizontal="@dimen/spacing_12"
    android:paddingVertical="@dimen/spacing_4"
    android:textSize="11sp"
    android:textStyle="bold"
    android:letterSpacing="0.08"
    android:fontFamily="@font/inter"
    android:background="@drawable/bg_badge_success"
    android:textColor="@color/color_success" />
```

```xml
<!-- res/drawable/bg_badge_success.xml  (clone for error / warning / info) -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/color_success_light" />
    <corners android:radius="@dimen/radius_badge" />
</shape>
```

Create 4 drawables total: `bg_badge_success`, `bg_badge_error`, `bg_badge_warning`, `bg_badge_info`.

---

## Dialog

```java
new MaterialAlertDialogBuilder(context, R.style.Dialog_Timsanbong)
    .setTitle("Tiêu đề")
    .setMessage("Nội dung mô tả.")
    .setPositiveButton("Xác nhận", (d, w) -> { /* action */ })
    .setNegativeButton("Huỷ", null)
    .show();
```

```xml
<!-- res/values/themes.xml -->
<style name="Dialog_Timsanbong" parent="ThemeOverlay.MaterialComponents.MaterialAlertDialog">
    <item name="shapeAppearanceOverlay">@style/ShapeAppearance.Dialog.Timsanbong</item>
    <item name="android:backgroundDimAmount">0.5</item>
</style>
<style name="ShapeAppearance.Dialog.Timsanbong" parent="">
    <item name="cornerFamily">rounded</item>
    <item name="cornerSize">@dimen/radius_card</item>
</style>
```

---

## BottomSheet

```java
// In BottomSheetDialogFragment
BottomSheetDialog dialog = (BottomSheetDialog) requireDialog();
dialog.getBehavior().setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 2);
```

```xml
<!-- res/values/themes.xml -->
<style name="BottomSheet_Timsanbong" parent="Theme.MaterialComponents.BottomSheetDialog">
    <item name="bottomSheetStyle">@style/Widget.BottomSheet.Timsanbong</item>
</style>
<style name="Widget.BottomSheet.Timsanbong" parent="Widget.MaterialComponents.BottomSheet.Modal">
    <item name="shapeAppearanceOverlay">@style/ShapeAppearance.BottomSheet.Timsanbong</item>
    <item name="android:background">@color/background_secondary</item>
</style>
<style name="ShapeAppearance.BottomSheet.Timsanbong" parent="">
    <item name="cornerFamily">rounded</item>
    <item name="cornerSizeTopLeft">@dimen/radius_card</item>
    <item name="cornerSizeTopRight">@dimen/radius_card</item>
</style>
```

Drag handle (place at top of sheet content):
```xml
<View
    android:layout_width="40dp"
    android:layout_height="4dp"
    android:layout_gravity="center_horizontal"
    android:layout_marginTop="@dimen/spacing_8"
    android:layout_marginBottom="@dimen/spacing_16"
    android:background="@color/text_hint" />
```

---

## Empty State

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="@dimen/spacing_40">

    <ImageView
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:src="@drawable/ic_empty_state"
        android:imageTintList="@color/border_gray"
        android:contentDescription="@null" />

    <TextView
        android:layout_marginTop="@dimen/spacing_16"
        android:textSize="18sp"
        android:textStyle="bold"
        android:fontFamily="@font/inter"
        android:textColor="@color/text_heading"
        android:gravity="center" />

    <TextView
        android:layout_marginTop="@dimen/spacing_8"
        android:textSize="14sp"
        android:fontFamily="@font/inter"
        android:textColor="@color/text_body"
        android:gravity="center" />

</LinearLayout>
```

---

## Ripple (non-button tappable views)

```xml
<!-- res/drawable/ripple_primary.xml -->
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/primary_light">
    <item android:id="@android:id/mask">
        <shape android:shape="rectangle">
            <corners android:radius="@dimen/radius_card" />
            <solid android:color="#FFFFFF" />
        </shape>
    </item>
</ripple>
```

Apply: `android:background="@drawable/ripple_primary"`
