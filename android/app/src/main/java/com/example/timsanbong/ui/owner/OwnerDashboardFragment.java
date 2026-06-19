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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// 1. Đổi tên class thành OwnerDashboardFragment cho đúng chuẩn điều hướng
public class OwnerDashboardFragment extends Fragment {

    private OwnerFieldViewModel fieldViewModel;
    private OwnerBookingViewModel bookingViewModel;
    private OwnerBookingAdapter bookingAdapter;
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
        fieldViewModel.loadFields();
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
        tvStatCompletedValue = view.findViewById(R.id.tvStatCompletedValue);
        tvStatCompletedChange = view.findViewById(R.id.tvStatCompletedChange);
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

        // 3. Các nút tính năng phụ (Tin nhắn / Thông báo) nếu chưa làm Fragment thì có thể giữ nguyên hoặc cập nhật sau
        view.findViewById(R.id.btnMessages).setOnClickListener(v -> {
            // Tạm thời giữ nguyên hoặc chuyển đổi tương tự nếu có MessagesFragment
            startActivity(new Intent(requireContext(), com.example.timsanbong.ui.customer.MessagesActivity.class));
        });

        view.findViewById(R.id.btnNotificationBell).setOnClickListener(v -> {
            // Tạm thời giữ nguyên hoặc chuyển đổi tương tự
            startActivity(new Intent(requireContext(), com.example.timsanbong.ui.customer.MessagesActivity.class));
        });
    }

    private void setupRecentBookings(View view) {
        RecyclerView rvRecentBookings = view.findViewById(R.id.rvRecentBookings);
        bookingAdapter = new OwnerBookingAdapter(new OwnerBookingAdapter.Listener() {
            @Override
            public void onCheckIn(Booking booking) {
                bookingViewModel.checkInBooking(booking.getId());
            }

            @Override
            public void onCollectRest(Booking booking) {
                bookingViewModel.checkOutBooking(booking.getId());
            }

            @Override
            public void onNoShow(Booking booking) {
                bookingViewModel.markNoShow(booking.getId());
            }
        });

        // Thay 'this' bằng 'requireContext()' cho LayoutManager
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentBookings.setAdapter(bookingAdapter);
    }

    // 6. Đổi Observers sử dụng 'getViewLifecycleOwner()' thay vì 'this' để tối ưu quản lý bộ nhớ của Fragment
    private void setupObservers() {
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

    private void renderStats() {
        int totalFields = currentFields.size();
        int totalSlots = 0;
        int waitingDeposit = 0;
        int needAction = 0;
        int completed = 0;
        BigDecimal collectedDeposit = BigDecimal.ZERO;

        for (Booking booking : currentBookings) {
            String status = safeStatus(booking.getStatus());
            if ("PENDING".equals(status)) {
                waitingDeposit++;
            } else if ("DEPOSIT_PAID".equals(status)) {
                needAction++;
                collectedDeposit = collectedDeposit.add(BigDecimal.valueOf(booking.getDepositAmount()));
            } else if ("CONFIRMED".equals(status)) {
                needAction++;
                collectedDeposit = collectedDeposit.add(BigDecimal.valueOf(booking.getDepositAmount()));
            } else if ("COMPLETED".equals(status)) {
                completed++;
                collectedDeposit = collectedDeposit.add(BigDecimal.valueOf(booking.getDepositAmount()));
            }
        }
        for (Field field : currentFields) {
            List<TimeSlotResponse> slots = field.getTimeSlots();
            totalSlots += slots == null ? 0 : slots.size();
        }

        tvOwnerName.setText(resolveOwnerName());
        tvOwnerAvatar.setText(resolveAvatar());
        tvGreeting.setText(resolveGreeting());
        tvRevenueValue.setText(formatMoney(collectedDeposit));
        tvRevenueChange.setText(totalSlots + " khung giờ · " + needAction + " cần xử lý");
        tvStatPendingValue.setText(String.valueOf(waitingDeposit));
        tvStatFieldsValue.setText(String.valueOf(totalFields));
        tvStatCompletedValue.setText(String.valueOf(completed));
        tvStatCompletedChange.setText(needAction + " cần xử lý");
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