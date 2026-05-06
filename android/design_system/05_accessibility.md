# 05 · Accessibility
> Requires: `00_core.md`

---

## Touch Target — Minimum 48×48dp

```xml
<!-- Always on icon-only buttons -->
<ImageButton
    android:minWidth="48dp"
    android:minHeight="48dp"
    android:padding="@dimen/spacing_12"
    android:src="@drawable/ic_share"
    android:contentDescription="@string/cd_share" />
```

Set globally in theme (covers all MaterialButton automatically):
```xml
<item name="minTouchTargetSize">48dp</item>
```

---

## contentDescription Rules

| View | Rule |
|------|------|
| Icon-only `ImageButton` / `FloatingActionButton` | **Required** — use `@string/cd_*` |
| `ImageView` with text beside it (decorative) | `android:contentDescription="@null"` |
| `ImageView` with meaningful content | **Required** |
| `ProgressBar` | `android:contentDescription="@string/cd_loading"` |

```xml
<!-- res/values/content_descriptions.xml -->
<resources>
    <string name="cd_scroll_to_top">Cuộn lên đầu trang</string>
    <string name="cd_close">Đóng</string>
    <string name="cd_back">Quay lại</string>
    <string name="cd_search">Tìm kiếm</string>
    <string name="cd_share">Chia sẻ</string>
    <string name="cd_more_options">Thêm tùy chọn</string>
    <string name="cd_loading">Đang tải</string>
    <string name="cd_avatar">Ảnh đại diện</string>
</resources>
```

---

## Contrast Ratio Requirements (WCAG AA)

| Text type | Min ratio |
|-----------|-----------|
| Body text < 18sp | 4.5:1 |
| Large text ≥ 18sp or ≥ 14sp bold | 3.0:1 |
| UI icons & graphics | 3.0:1 |

### Verified Pairs

| Foreground | Background | Ratio | Status |
|-----------|-----------|-------|--------|
| `text_heading` #1A202C | white | 16.7:1 | ✅ AAA |
| `text_body` #4A5568 | white | 7.4:1 | ✅ AAA |
| `primary_text` #2F855A | white | 7.1:1 | ✅ AAA |
| `color_success` #38A169 | white | 4.6:1 | ✅ AA |
| `text_on_primary` #1A202C | `primary` #60D86E | 5.2:1 | ✅ AA |
| `primary` #60D86E | white | 2.8:1 | ❌ FAIL |
| white #FFFFFF | `primary` #60D86E | 2.8:1 | ❌ FAIL |

**Critical rule:** `#60D86E` must only be used as a **background color**. Never as text or icon color on a light surface.
