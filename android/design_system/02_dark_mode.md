# 02 · Dark Mode
> Requires: `00_core.md`

---

## Directory Structure

```
res/
├── values/
│   ├── colors.xml       ← Light (defined in 00_core.md)
│   └── themes.xml       ← Theme attrs (below)
└── values-night/
    └── colors.xml       ← Dark overrides (below)
```

---

## `res/values-night/colors.xml`

```xml
<resources>
    <color name="primary">#4DC95C</color>
    <color name="primary_dark">#3AB54A</color>
    <color name="primary_light">#1A3D1E</color>
    <color name="primary_text">#68D391</color>
    <color name="text_on_primary">#0F2415</color>

    <color name="text_heading">#F7FAFC</color>
    <color name="text_body">#CBD5E0</color>
    <color name="text_hint">#718096</color>

    <color name="background_primary">#1A202C</color>
    <color name="background_secondary">#2D3748</color>
    <color name="background_tertiary">#374151</color>
    <color name="border_gray">#4A5568</color>
    <color name="surface_overlay">#33FFFFFF</color>

    <color name="color_error">#FC8181</color>
    <color name="color_error_light">#2D1515</color>
    <color name="color_success">#68D391</color>
    <color name="color_success_light">#12291A</color>
    <color name="color_warning">#F6E05E</color>
    <color name="color_warning_light">#2D2208</color>
    <color name="color_info">#63B3ED</color>
    <color name="color_info_light">#0A1929</color>
</resources>
```

---

## `res/values/themes.xml`

```xml
<resources>
    <style name="Theme.Timsanbong" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_dark</item>
        <item name="colorSecondary">@color/primary</item>
        <item name="colorOnPrimary">@color/text_on_primary</item>
        <item name="android:windowBackground">@color/background_primary</item>
        <item name="colorSurface">@color/background_secondary</item>
        <item name="android:colorBackground">@color/background_primary</item>
        <item name="colorOnSurface">@color/text_heading</item>
        <item name="android:textColorPrimary">@color/text_heading</item>
        <item name="android:textColorSecondary">@color/text_body</item>
        <item name="android:textColorHint">@color/text_hint</item>
        <item name="colorError">@color/color_error</item>
        <item name="minTouchTargetSize">48dp</item>
    </style>
</resources>
```

---

## Usage Rule

```xml
<!-- ✅ Correct — auto-switches Light/Dark -->
<TextView android:textColor="?attr/android:textColorPrimary"
          android:background="?attr/colorSurface" />

<!-- ❌ Wrong — hardcoded, breaks Dark Mode -->
<TextView android:textColor="#1A202C"
          android:background="#F8FAF8" />
```

**Rule:** Every color in layout XML must be `@color/token_name` (system picks light/night automatically) or `?attr/` — never a raw hex value.

---

## Checklist when adding new tokens

- [ ] Add to `res/values/colors.xml` (light value)
- [ ] Add to `res/values-night/colors.xml` (dark value)
- [ ] Reference in layout via `@color/token_name` — never `?attr/` for custom tokens
