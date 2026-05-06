# 00 · Core Tokens
> Load this file for EVERY task. It contains all design tokens required to generate any UI code.

---

## Colors — `res/values/colors.xml`

```xml
<resources>
    <!-- Brand -->
    <color name="primary">#60D86E</color>
    <color name="primary_dark">#45C45A</color>
    <color name="primary_light">#E8F9EB</color>
    <color name="primary_text">#2F855A</color>      <!-- green text on light bg — 7.1:1 ✅ -->
    <color name="text_on_primary">#1A202C</color>   <!-- text/icon ON primary bg — 5.2:1 ✅ -->

    <!-- Text -->
    <color name="text_heading">#1A202C</color>
    <color name="text_body">#4A5568</color>
    <color name="text_hint">#A0AEC0</color>

    <!-- Background -->
    <color name="background_primary">#FFFFFF</color>
    <color name="background_secondary">#F8FAF8</color>
    <color name="background_tertiary">#F0F4F0</color>
    <color name="border_gray">#F3F4F6</color>
    <color name="surface_overlay">#1F000000</color>

    <!-- Semantic -->
    <color name="color_error">#E53E3E</color>
    <color name="color_error_light">#FFF5F5</color>
    <color name="color_success">#38A169</color>
    <color name="color_success_light">#F0FFF4</color>
    <color name="color_warning">#D69E2E</color>
    <color name="color_warning_light">#FFFBEB</color>
    <color name="color_info">#3182CE</color>
    <color name="color_info_light">#EBF8FF</color>
</resources>
```

---

## Dimens — `res/values/dimens.xml`

```xml
<resources>
    <!-- Radius -->
    <dimen name="radius_pill">100dp</dimen>
    <dimen name="radius_card">24dp</dimen>
    <dimen name="radius_nav">16dp</dimen>
    <dimen name="radius_badge">100dp</dimen>
    <dimen name="radius_image">12dp</dimen>

    <!-- Elevation -->
    <dimen name="shadow_sm">2dp</dimen>
    <dimen name="shadow_md">8dp</dimen>
    <dimen name="shadow_lg">16dp</dimen>

    <!-- Touch -->
    <dimen name="touch_target_min">48dp</dimen>

    <!-- Spacing scale -->
    <dimen name="spacing_2">2dp</dimen>
    <dimen name="spacing_4">4dp</dimen>
    <dimen name="spacing_8">8dp</dimen>
    <dimen name="spacing_12">12dp</dimen>
    <dimen name="spacing_16">16dp</dimen>
    <dimen name="spacing_20">20dp</dimen>
    <dimen name="spacing_24">24dp</dimen>
    <dimen name="spacing_32">32dp</dimen>
    <dimen name="spacing_40">40dp</dimen>
    <dimen name="spacing_48">48dp</dimen>
    <dimen name="spacing_64">64dp</dimen>
</resources>
```

---

## Typography

Font file: `res/font/inter.ttf` → reference as `android:fontFamily="@font/inter"`

| Role | Size | Weight | Letter Spacing |
|------|------|--------|----------------|
| Display | 28sp | bold | -0.02 |
| Heading 1 | 22sp | bold | -0.02 |
| Heading 2 | 18sp | bold | -0.01 |
| Body | 14sp | normal | 0 |
| Caption | 12sp | normal | 0 |
| Badge label | 11sp | bold | 0.08 |

---

## Spacing Usage

| Context | Token |
|---------|-------|
| Card inner padding | `spacing_24` |
| Screen horizontal padding | `spacing_16` |
| Button padding H / V | `spacing_20` / `spacing_12` |
| Gap between cards | `spacing_12` |
| Icon ↔ text gap | `spacing_8` |
| Bottom nav margin bottom | `spacing_24` |

---

## Hard Rules (apply to every file)

- **Never** write hex directly in layout XML — always `@color/token`
- **Never** write raw `dp` in layout XML — always `@dimen/spacing_*`
- **Never** use `#60D86E` as text color — only as background
- **Always** use `@font/inter` on every TextView
- **Always** add `android:interpolator` to every `<objectAnimator>`
