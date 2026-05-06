# 06 · AI Agent Rules
> This file is addressed directly to AI code generation agents.
> Load this file alongside task-specific files on every generation task.

---

## Which files to load per task

| Task | Load |
|------|------|
| Any layout XML | `00_core` + `06_agent_rules` |
| New component | `00_core` + `01_components` + `06_agent_rules` |
| Dark mode / theming | `00_core` + `02_dark_mode` + `06_agent_rules` |
| Navigation / screen layout | `00_core` + `03_navigation_layout` + `06_agent_rules` |
| Animation / scroll behavior | `00_core` + `04_animation` + `06_agent_rules` |
| Accessibility audit | `00_core` + `05_accessibility` + `06_agent_rules` |
| Full screen (Activity/Fragment) | all files |

---

## Pre-generation Checklist

Run through this before outputting any code:

**Layout XML**
- [ ] All colors → `@color/token_name` — no raw hex
- [ ] All spacing/radius → `@dimen/spacing_*` or `@dimen/radius_*` — no raw `dp`
- [ ] All fonts → `android:fontFamily="@font/inter"`
- [ ] Every interactive view has `minHeight`/`minWidth` ≥ 48dp
- [ ] Every icon-only view has `android:contentDescription="@string/cd_*"`
- [ ] `MaterialButton` text color is `@color/btn_primary_text_color` — NOT white
- [ ] No `GradientDrawable` used for backgrounds

**Java**
- [ ] Every `ObjectAnimator` / `ObjectAnimator` set has an interpolator declared
- [ ] Decorative animations check `ANIMATOR_DURATION_SCALE` before starting
- [ ] Form error is cleared with `setError(null)` after successful validation

**Dark Mode**
- [ ] New color tokens added to BOTH `values/colors.xml` AND `values-night/colors.xml`
- [ ] No hardcoded hex in any layout attribute

---

## Naming Conventions

| Resource | Pattern | Example |
|----------|---------|---------|
| Layout | `activity_*` `fragment_*` `item_*` `view_*` | `item_product_card.xml` |
| Drawable background | `bg_*` | `bg_badge_success.xml` |
| Drawable icon | `ic_*` | `ic_keyboard_arrow_up.xml` |
| Drawable shape | `shape_*` | `shape_status_dot.xml` |
| Drawable ripple | `ripple_*` | `ripple_primary.xml` |
| Color selector | `*_color_state` `*_bg_state` | `nav_item_color_state.xml` |
| Animator | `*_animator` | `button_scale_animator.xml` |
| String — content description | `cd_*` | `cd_scroll_to_top` |
| String — error message | `error_*` | `error_invalid_email` |
| Style — Dialog | `Dialog_*` | `Dialog_Timsanbong` |
| Style — BottomSheet | `BottomSheet_*` | `BottomSheet_Timsanbong` |
| Style — ShapeAppearance | `ShapeAppearance.*` | `ShapeAppearance.Dialog.Timsanbong` |

---

## Prohibited Patterns

Never generate any of the following:

```xml
<!-- ❌ Hardcoded hex color -->
android:textColor="#1A202C"

<!-- ❌ Hardcoded dp spacing -->
android:padding="16dp"

<!-- ❌ White text on primary button -->
<MaterialButton android:textColor="#FFFFFF" app:backgroundTint="@color/primary" />

<!-- ❌ Primary color as text -->
<TextView android:textColor="@color/primary" />

<!-- ❌ Touch target too small -->
<ImageButton android:layout_width="24dp" android:layout_height="24dp" />

<!-- ❌ Missing contentDescription on icon-only view -->
<ImageButton android:src="@drawable/ic_close" />

<!-- ❌ GradientDrawable background -->
<GradientDrawable ... />

<!-- ❌ Emoji in UI text -->
<TextView android:text="✅ Thành công" />

<!-- ❌ Animator without interpolator -->
<objectAnimator android:propertyName="scaleX" android:duration="200" android:valueTo="0.95" />

<!-- ❌ Hardcoded hex in night colors file -->
<color name="text_heading">#1A202C</color>  <!-- in values-night/ — must be the DARK value -->
```

---

## Output Format

When generating code, always output in this order:
1. Layout XML file(s)
2. Drawable XML file(s) if new ones are needed
3. Color selector XML file(s) if new ones are needed
4. Animator XML file(s) if new ones are needed
5. Java code
6. Strings to add to `strings.xml` / `content_descriptions.xml`
7. Style entries to add to `themes.xml`

Label each block clearly with its file path, e.g.:
```
<!-- res/layout/fragment_home.xml -->
```
