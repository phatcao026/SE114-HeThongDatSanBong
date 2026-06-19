package com.example.timsanbong.ui.owner;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

// 1. Chuyển đổi từ AppCompatActivity sang Fragment
public class OwnerBookingFragment extends Fragment {
    private OwnerBookingViewModel viewModel;
    private OwnerBookingAdapter adapter;
    private EditText etSearchBookings;
    private TextView tvTotalBookings;
    private TextView tvDepositCollected;
    private TextView tvPendingBadge;
    private View emptyState;
    private View btnClearFilters;

    // 2. Nạp giao diện fragment_owner_booking vào hệ thống
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_booking, container, false);
    }

    // 3. Khởi tạo logic và ánh xạ các phần tử UI sau khi view đã được dựng xong
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OwnerBookingViewModel.class);

        bindViews(view);
        setupTabs(view);
        setupRecyclerView(view);
        setupSearch();
        setupActions(view);
        setupObservers();

        /* Bỏ dòng thiết lập thanh điều hướng cục bộ cũ:
           new OwnerNavBarManager(this, OwnerNavBarManager.ITEM_BOOKINGS).setup();
           Vì giao diện Bottom Nav hiện tại đã chạy tập trung tại OwnerMainActivity.
        */

        viewModel.loadBookings();
    }

    // 4. Cấu hình tìm kiếm phần tử thông qua biến gốc 'view' của Fragment
    private void bindViews(View view) {
        etSearchBookings = view.findViewById(R.id.etSearchBookings);
        tvTotalBookings = view.findViewById(R.id.tvTabAllCount);
        tvDepositCollected = view.findViewById(R.id.tvTabCompletedCount);
        tvPendingBadge = view.findViewById(R.id.tvPendingBadge);
        emptyState = view.findViewById(R.id.llEmptyState);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvBookings = view.findViewById(R.id.rvBookingList);
        adapter = new OwnerBookingAdapter(new OwnerBookingAdapter.Listener() {
            @Override
            public void onCheckIn(Booking booking) {
                confirmAction(
                        "Xác nhận check-in",
                        "Xác nhận khách đã đến cho đơn #" + booking.getId() + "?",
                        () -> viewModel.checkInBooking(booking.getId()));
            }

            @Override
            public void onCollectRest(Booking booking) {
                showPaymentMethodDialog(booking);
            }

            @Override
            public void onNoShow(Booking booking) {
                confirmAction(
                        "Đánh dấu no-show",
                        "Khách không đến sẽ khiến đơn #" + booking.getId() + " bị hủy. Tiếp tục?",
                        () -> viewModel.markNoShow(booking.getId()));
            }
        });

        // Thay 'this' bằng ngữ cảnh an toàn 'requireContext()'
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookings.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchBookings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s == null ? "" : s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupActions(View view) {
        View btnDateFilter = view.findViewById(R.id.btnDateFilter);
        btnDateFilter.setOnClickListener(v -> openDatePicker());
        if (btnClearFilters != null) {
            btnClearFilters.setOnClickListener(v -> clearFilters());
        }
    }

    private void setupTabs(View view) {
        setupTab(view, R.id.tabAll, "ALL");
        setupTab(view, R.id.tabPending, "PENDING");
        setupTab(view, R.id.tabConfirmed, "CONFIRMED");
        setupTab(view, R.id.tabCompleted, "COMPLETED");
        setupTab(view, R.id.tabCancelled, "CANCELLED");
        // Initial tab state will be set by observer
    }

    private void setupTab(View view, int viewId, String status) {
        View tab = view.findViewById(viewId);
        if (tab != null) {
            tab.setOnClickListener(v -> {
                viewModel.setSelectedStatus(status);
                updateTabState(status); // Update UI immediately
            });
        }
    }

    private void updateTabState(String selectedStatus) {
        setTabSelected(R.id.tabAll, "ALL".equals(selectedStatus));
        setTabSelected(R.id.tabPending, "PENDING".equals(selectedStatus));
        setTabSelected(R.id.tabConfirmed, "CONFIRMED".equals(selectedStatus));
        setTabSelected(R.id.tabCompleted, "COMPLETED".equals(selectedStatus));
        setTabSelected(R.id.tabCancelled, "CANCELLED".equals(selectedStatus));
    }

    private void setTabSelected(int viewId, boolean selected) {
        if (getView() != null) {
            View tab = getView().findViewById(viewId);
            if (tab != null) {
                tab.setSelected(selected);
            }
        }
    }

    // 5. Đồng bộ hóa bộ lắng nghe bằng 'getViewLifecycleOwner()' thay vì 'this'
    private void setupObservers() {
        viewModel.getFilteredBookings().observe(getViewLifecycleOwner(), bookings -> {
            if (adapter != null) {
                adapter.submitList(bookings);
            }
            if (emptyState != null) {
                emptyState.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.getBookingSummary().observe(getViewLifecycleOwner(), summary -> {
            if (tvTotalBookings != null) tvTotalBookings.setText(String.valueOf(summary.totalBookings));
            if (tvDepositCollected != null) tvDepositCollected.setText(summary.formatDepositCollected());
            if (tvPendingBadge != null) {
                tvPendingBadge.setText(summary.formatPendingBadge());
                tvPendingBadge.setVisibility(summary.pendingCount > 0 ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.getBusyBookingId().observe(getViewLifecycleOwner(), busyId -> {
            if (adapter != null) {
                adapter.setActionState(Boolean.TRUE.equals(viewModel.getLoading().getValue()),
                        busyId == null ? -1L : busyId);
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (adapter != null) {
                long busyId = viewModel.getBusyBookingId().getValue() == null ? -1L : viewModel.getBusyBookingId().getValue();
                adapter.setActionState(Boolean.TRUE.equals(loading), busyId);
            }
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty() && getContext() != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 6. Cấu hình Dialog chọn ngày với Context là requireContext()
    private void openDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    viewModel.setSelectedDate(selectedDate);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void clearFilters() {
        viewModel.clearFilters();
        if (etSearchBookings != null) {
            etSearchBookings.setText("");
        }
        updateTabState("ALL"); // Reset tab to ALL
    }

    // 7. Cấu hình Material Dialog xác nhận hành động bằng requireContext()
    private void confirmAction(String title, String message, Runnable action) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> action.run())
                .show();
    }

    private void showPaymentMethodDialog(Booking booking) {
        String[] paymentMethods = {"Tiền mặt", "Chuyển khoản (Ví MOMO, Banking)"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn hình thức thanh toán")
                .setItems(paymentMethods, (dialog, which) -> {
                    String method = (which == 0) ? "CASH" : "MOMO"; // Assuming "MOMO" for transfer
                    confirmAction(
                            "Thu nốt tiền",
                            "Ghi nhận đã thu đủ tiền cho đơn #" + booking.getId() + " bằng hình thức " + paymentMethods[which] + "?",
                            () -> viewModel.checkOutBooking(booking.getId(), method)
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
