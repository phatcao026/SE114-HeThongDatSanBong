package com.example.timsanbong.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.TimeSlotResponse;
import com.example.timsanbong.utils.SessionManager;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// 1. Đổi tên class thành OwnerDashboardFragment cho đúng chuẩn điều hướng
public class OwnerDashboardFragment extends Fragment {

    private OwnerFieldViewModel fieldViewModel;
    private OwnerBookingViewModel bookingViewModel;
    private RecentBookingAdapter bookingAdapter;
    private final List<Field> currentFields = new ArrayList<>();
    private final List<Booking> currentBookings = new ArrayList<>();

    private TextView tvOwnerName;
    private TextView tvGreeting;
    private TextView tvOwnerAvatar;
    private TextView tvRevenueValue;
    private TextView tvRevenueChange;
    private TextView tvStatPendingValue;
    private TextView tvStatFieldsValue;
    private TextView tvStatCompletedValue;
    private TextView tvStatCompletedChange;
    private TextView tvNotificationBadge;
    private View recentBookingsEmptyState;

    // 2. Fragment KHÔNG dùng setContentView trong onCreate. Thay vào đó dùng onCreateView để nạp layout XML
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }

    // 3. Toàn bộ logic ánh xạ View và khởi tạo dữ liệu sẽ được đưa vào onViewCreated sau khi giao diện đã sẵn sàng
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel gắn với Fragment này
        fieldViewModel = new ViewModelProvider(this).get(OwnerFieldViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(OwnerBookingViewModel.class);

        // Truyền biến 'view' vào để ánh xạ các thành phần giao diện con
        bindViews(view);
        setupActions(view);
        setupRecentBookings(view);
        setupObservers();

        /* LƯU Ý QUAN TRỌNG:
           Bỏ dòng: new OwnerNavBarManager(this, OwnerNavBarManager.ITEM_DASHBOARD).setup();
           Vì hiện tại thanh điều hướng Bottom Nav đã được quản lý cố định tập trung tại OwnerMainActivity rồi!
        */

        bindSessionUser();
        
        // Logic lấy ngày hôm nay đúng chuẩn để tải dữ liệu sân
        Calendar cal = Calendar.getInstance();
        String today = String.format(Locale.US, "%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        fieldViewModel.loadFields(today);

        bookingViewModel.loadBookings();
    }

    // 4. Sửa lại findViewById thành view.findViewById
    private void bindViews(View view) {
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvGreeting = view.findViewById(R.id.tvGreetingMorning);
        tvOwnerAvatar = view.findViewById(R.id.tvOwnerAvatar);
        tvRevenueValue = view.findViewById(R.id.tvRevenueValue);
        tvRevenueChange = view.findViewById(R.id.tvRevenueChange);
        tvStatPendingValue = view.findViewById(R.id.tvStatPendingValue);
        tvStatFieldsValue = view.findViewById(R.id.tvStatFieldsValue);
       tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);
        recentBookingsEmptyState = view.findViewById(R.id.tvOwnerEmptyRecentBookings);
    }

    // 5. Thay thế từ khóa 'this' làm Context bằng 'requireContext()' khi chuyển màn hình (Intent)
    private void setupActions(View view) {
        // 1. Khi nhấn nút Thêm Sân hoặc Xem Sân -> Chuyển sang Tab Quản lý Sân bóng
        view.findViewById(R.id.btnNewField).setOnClickListener(v -> {
            if (getActivity() instanceof OwnerMainActivity) {
                ((OwnerMainActivity) getActivity()).switchToFieldsTab();
            }
        });

        // 2. Khi nhấn nút Lịch đặt hoặc Xem tất cả đơn -> Chuyển sang Tab Đơn đặt sân
        view.findViewById(R.id.btnCalendar).setOnClickListener(v -> {
            if (getActivity() instanceof OwnerMainActivity) {
                ((OwnerMainActivity) getActivity()).switchToBookingsTab();
            }
        });

        view.findViewById(R.id.btnSeeAllBookings).setOnClickListener(v -> {
            if (getActivity() instanceof OwnerMainActivity) {
                ((OwnerMainActivity) getActivity()).switchToBookingsTab();
            }
        });

        view.findViewById(R.id.btnNotificationBell).setOnClickListener(v -> {
            // Open NotificationsActivity
            try {
                startActivity(new Intent(requireContext(), com.example.timsanbong.ui.customer.NotificationsActivity.class));
            } catch (Exception ex) {
                showMessage("Không thể mở thông báo");
            }
        });

        // Logout button: confirm then clear session and navigate to LoginActivity
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        SessionManager sessionManager = new SessionManager(requireContext());
                        sessionManager.clearSession();
                        android.content.Intent intent = new android.content.Intent(requireContext(), com.example.timsanbong.ui.auth.LoginActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void setupRecentBookings(View view) {
        RecyclerView rvRecentBookings = view.findViewById(R.id.rvRecentBookings);
        bookingAdapter = new RecentBookingAdapter(new RecentBookingAdapter.Listener() {
            @Override
            public void onItemClick(Booking booking) {
                if (getActivity() instanceof OwnerMainActivity) {
                    ((OwnerMainActivity) getActivity()).switchToBookingsTab();
                }
            }
        });

        // Thay 'this' bằng 'requireContext()' cho LayoutManager
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentBookings.setAdapter(bookingAdapter);
    }

    // 6. Đổi Observers sử dụng 'getViewLifecycleOwner()' thay vì 'this' để tối ưu quản lý bộ nhớ của Fragment
    private void setupObservers() {
        View loadingBar = getActivity() != null ? getActivity().findViewById(R.id.ownerLoadingBar) : null;
        View fragmentRootView = getView();

        androidx.lifecycle.Observer<Boolean> loadingObserver = isLoading -> {
            boolean isFieldLoading = fieldViewModel.getLoading().getValue() != null && fieldViewModel.getLoading().getValue();
            boolean isBookingLoading = bookingViewModel.getLoading().getValue() != null && bookingViewModel.getLoading().getValue();
            boolean loading = isFieldLoading || isBookingLoading;

            if (loadingBar != null) loadingBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (fragmentRootView != null) {
                fragmentRootView.setAlpha(loading ? 0.4f : 1.0f);
                fragmentRootView.setEnabled(!loading);
            }
        };

        fieldViewModel.getLoading().observe(getViewLifecycleOwner(), loadingObserver);
        bookingViewModel.getLoading().observe(getViewLifecycleOwner(), loadingObserver);

        fieldViewModel.getFields().observe(getViewLifecycleOwner(), fields -> {
            currentFields.clear();
            if (fields != null) {
                currentFields.addAll(fields);
            }
            renderStats();
        });
        bookingViewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            currentBookings.clear();
            if (bookings != null) {
                currentBookings.addAll(bookings);
            }
            bookingAdapter.submitList(getRecentBookings(currentBookings, 5));
            if (recentBookingsEmptyState != null) {
                recentBookingsEmptyState.setVisibility(currentBookings.isEmpty() ? View.VISIBLE : View.GONE);
            }
            renderStats();
        });
        fieldViewModel.getMessage().observe(getViewLifecycleOwner(), this::showMessage);
        bookingViewModel.getMessage().observe(getViewLifecycleOwner(), this::showMessage);
    }

    // Trong file OwnerDashboardFragment.java, cập nhật hàm renderStats:
    private void renderStats() {
        int totalFields = currentFields.size();
        int waitingDeposit = 0;
        int needAction = 0;
        int completed = 0;
        BigDecimal collectedDeposit = BigDecimal.ZERO;

        for (Booking booking : currentBookings) {
            String status = safeStatus(booking.getStatus());
            if ("PENDING".equals(status)) {
                waitingDeposit++;
            } else if ("DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status)) {
                needAction++;
                collectedDeposit = collectedDeposit.add(BigDecimal.valueOf(booking.getDepositAmount()));
            } else if ("COMPLETED".equals(status)) {
                completed++;
                collectedDeposit = collectedDeposit.add(BigDecimal.valueOf(booking.getDepositAmount()));
            }
        }

        tvOwnerName.setText(resolveOwnerName());
        tvOwnerAvatar.setText(resolveAvatar());
        tvGreeting.setText(resolveGreeting());

        // Hiển thị tổng tiền cọc
        tvRevenueValue.setText(formatMoney(collectedDeposit));

        // GỘP THÔNG TIN: Thành công và Cần xử lý
        tvRevenueChange.setText(completed + " thành công · " + needAction + " cần xử lý");

        // Cập nhật 2 card còn lại
        tvStatPendingValue.setText(String.valueOf(waitingDeposit));
        tvStatFieldsValue.setText(String.valueOf(totalFields));

        // Ẩn badge thông báo nếu không dùng
        tvNotificationBadge.setVisibility(View.GONE);
    }


    private List<Booking> getRecentBookings(List<Booking> bookings, int limit) {
        List<Booking> recent = new ArrayList<>(bookings);
        Collections.sort(recent, (a, b) -> safeString(b.getBookingDate()).compareTo(safeString(a.getBookingDate())));
        if (recent.size() > limit) {
            return recent.subList(0, limit);
        }
        return recent;
    }

    // 7. Thay 'this' bằng 'requireContext()' khi gọi SessionManager
    private void bindSessionUser() {
        try {
            String userJson = new SessionManager(requireContext()).getUserJson();
            if (userJson == null || userJson.trim().isEmpty()) {
                tvOwnerName.setText("Chủ sân");
                tvOwnerAvatar.setText("O");
                return;
            }
            JSONObject user = new JSONObject(userJson);
            String fullName = user.optString("fullName", "Chủ sân");
            tvOwnerName.setText(fullName);
            tvOwnerAvatar.setText(fullName.isEmpty() ? "O" : fullName.substring(0, 1).toUpperCase(Locale.US));
        } catch (Exception exception) {
            tvOwnerName.setText("Chủ sân");
            tvOwnerAvatar.setText("O");
        }
    }

    private String resolveOwnerName() {
        CharSequence current = tvOwnerName.getText();
        return current == null ? "Chủ sân" : current.toString();
    }

    private String resolveAvatar() {
        CharSequence current = tvOwnerAvatar.getText();
        return current == null ? "O" : current.toString();
    }

    private String resolveGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return "Chào buổi sáng";
        }
        if (hour < 18) {
            return "Chào buổi chiều";
        }
        return "Chào buổi tối";
    }

    private String formatMoney(BigDecimal amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " ₫";
    }

    private String safeStatus(String status) {
        return status == null ? "" : status.toUpperCase(Locale.US);
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    // 8. Đảm bảo Context không null trước khi thông báo Toast
    private void showMessage(String message) {
        if (message != null && !message.trim().isEmpty() && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
