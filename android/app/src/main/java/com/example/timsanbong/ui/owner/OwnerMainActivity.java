package com.example.timsanbong.ui.owner;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.PushNotificationManager;

public class OwnerMainActivity extends AppCompatActivity {

    // Khai báo các nút bấm (LinearLayout cha)
    private LinearLayout navDashboard, navFields, navBookings;

    // Khai báo các thành phần bên trong để đổi trạng thái UI
    private FrameLayout wrapDashboard, wrapFields, wrapBookings;
    private ImageView ivDashboard, ivFields, ivBookings;
    private TextView tvDashboard, tvFields, tvBookings;

    // Màu sắc trạng thái Active và Inactive
    private int colorActive;
    private int colorInactive = Color.parseColor("#9CA3AF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);
        PushNotificationManager.prepareForAuthenticatedUser(this);

        // 1. Khởi tạo màu sắc từ Resources
        colorActive = ContextCompat.getColor(this, R.color.green_primary);

        // 2. Ánh xạ các View từ file layout được include
        initViews();

        // 3. Hiển thị Fragment mặc định đầu tiên khi mở app (Dashboard)
        if (savedInstanceState == null) {
            replaceFragment(new OwnerDashboardFragment()); // Khởi tạo Fragment Dashboard của bạn
            setTabSelected(1);
        }

        // 4. Thiết lập sự kiện click cho từng tab
        navDashboard.setOnClickListener(v -> {
            replaceFragment(new OwnerDashboardFragment());
            setTabSelected(1);
        });

        navFields.setOnClickListener(v -> {
            replaceFragment(new OwnerFieldsFragment()); // Khởi tạo Fragment Sân/Cửa hàng của bạn
            setTabSelected(2);
        });

        navBookings.setOnClickListener(v -> {
            replaceFragment(new OwnerBookingFragment()); // Khởi tạo Fragment Lịch đặt của bạn
            setTabSelected(3);
        });
    }

    /**
     * Hàm ánh xạ các thành phần giao diện
     */
    private void initViews() {
        // Nút cha LinearLayout
        navDashboard = findViewById(R.id.ownerNavDashboard);
        navFields = findViewById(R.id.ownerNavFields);
        navBookings = findViewById(R.id.ownerNavBookings);

        // Khung nền bao quanh Icon (FrameLayout viên thuốc)
        wrapDashboard = findViewById(R.id.wrapOwnerNavDashboard);
        wrapFields = findViewById(R.id.wrapOwnerNavFields);
        wrapBookings = findViewById(R.id.wrapOwnerNavBookings);

        // Các Icon tượng trưng (ImageView)
        ivDashboard = findViewById(R.id.ivOwnerNavDashboard);
        ivFields = findViewById(R.id.ivOwnerNavFields);
        ivBookings = findViewById(R.id.ivOwnerNavBookings);

        // Các text nhãn phía dưới (TextView)
        tvDashboard = findViewById(R.id.tvOwnerNavDashboard);
        tvFields = findViewById(R.id.tvOwnerNavFields);
        tvBookings = findViewById(R.id.tvOwnerNavBookings);
    }

    /**
     * Hàm hoán đổi Fragment động trong ContainerView
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right);
        transaction.replace(R.id.ownerFragmentContainer, fragment);
        transaction.commit();
    }

    /**
     * Hàm xử lý thay đổi giao diện (Màu sắc, Background viên thuốc) khi Tab được chọn
     * @param tabIndex: 1 = Dashboard, 2 = Fields, 3 = Bookings
     */
    private void setTabSelected(int tabIndex) {
        // ---- Reset tất cả các Tab về trạng thái Inactive (Mờ đi) ----

        // Tab Dashboard
        wrapDashboard.setBackgroundResource(android.R.color.transparent);
        tvDashboard.setTypeface(null, Typeface.NORMAL);

        // Tab Fields
        wrapFields.setBackgroundResource(android.R.color.transparent);
        tvFields.setTypeface(null, Typeface.NORMAL);

        // Tab Bookings
        wrapBookings.setBackgroundResource(android.R.color.transparent);
        tvBookings.setTypeface(null, Typeface.NORMAL);


        // ---- Bật trạng thái Active cho Tab được bấm ----
        switch (tabIndex) {
            case 1: // Chọn Dashboard
                wrapDashboard.setBackgroundResource(R.drawable.bg_nav_pill_active);
                ivDashboard.setImageTintList(ColorStateList.valueOf(colorActive));
                tvDashboard.setTypeface(null, Typeface.BOLD); // Chữ đậm lên
                break;

            case 2: // Chọn Fields
                wrapFields.setBackgroundResource(R.drawable.bg_nav_pill_active);
                ivFields.setImageTintList(ColorStateList.valueOf(colorActive));
                tvFields.setTypeface(null, Typeface.BOLD);
                break;

            case 3: // Chọn Bookings
                wrapBookings.setBackgroundResource(R.drawable.bg_nav_pill_active);
                ivBookings.setImageTintList(ColorStateList.valueOf(colorActive));
                tvBookings.setTypeface(null, Typeface.BOLD);
                break;
        }
    }
    public void switchToFieldsTab() {
        if (navFields != null) {
            navFields.performClick(); // Gọi lệnh bấm vào LinearLayout điều hướng
        }
    }

    public void switchToBookingsTab() {
        if (navBookings != null) {
            navBookings.performClick(); // Gọi lệnh bấm vào LinearLayout điều hướng
        }
    }
}
