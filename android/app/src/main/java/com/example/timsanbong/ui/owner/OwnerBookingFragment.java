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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// 1. Chuyển đổi từ AppCompatActivity sang Fragment
public class OwnerBookingFragment extends Fragment {
    private OwnerBookingViewModel viewModel;
    private OwnerBookingAdapter adapter;
    private final List<Booking> allBookings = new ArrayList<>();
    private final List<Booking> visibleBookings = new ArrayList<>();
    private EditText etSearchBookings;
    private TextView tvTotalBookings;
    private TextView tvDepositCollected;
    private TextView tvPendingBadge;
    private View emptyState;
    private final TextView tvEmptyBookings = null;
    private View btnClearFilters;
    private String selectedDate;
    private String selectedStatus = "ALL";
    private String query = "";

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
                confirmAction(
                        "Thu nốt tiền",
                        "Ghi nhận đã thu đủ tiền cho đơn #" + booking.getId() + "?",
                        () -> viewModel.checkOutBooking(booking.getId()));
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
                query = s == null ? "" : s.toString().trim();
                applyFilters();
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
        updateTabState();
    }

    private void setupTab(View view, int viewId, String status) {
        View tab = view.findViewById(viewId);
        if (tab != null) {
            tab.setOnClickListener(v -> {
                selectedStatus = status;
                updateTabState();
                applyFilters();
            });
        }
    }

    private void updateTabState() {
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
        viewModel.getBookings().observe(getViewLifecycleOwner(), bookings -> {
            allBookings.clear();
            if (bookings != null) {
                allBookings.addAll(bookings);
            }
            applyFilters();
            updateSummary();
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

    private void applyFilters() {
        visibleBookings.clear();
        for (Booking booking : allBookings) {
            if (!matchesStatus(booking)) {
                continue;
            }
            if (!matchesDate(booking)) {
                continue;
            }
            if (!matchesQuery(booking)) {
                continue;
            }
            visibleBookings.add(booking);
        }
        Collections.sort(visibleBookings, (a, b) -> {
            int dateCompare = safeString(a.getBookingDate()).compareTo(safeString(b.getBookingDate()));
            if (dateCompare != 0) {
                return dateCompare;
            }
            return Long.compare(b.getId(), a.getId());
        });
        if (adapter != null) {
            adapter.submitList(visibleBookings);
        }
        if (emptyState != null) {
            emptyState.setVisibility(visibleBookings.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateSummary() {
        if (tvTotalBookings == null || tvDepositCollected == null || tvPendingBadge == null) return;

        tvTotalBookings.setText(String.valueOf(allBookings.size()));
        double depositSum = 0;
        int pendingCount = 0;
        for (Booking booking : allBookings) {
            String status = safeStatus(booking);
            if ("PENDING".equals(status)) {
                pendingCount++;
            }
            if ("DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status) || "COMPLETED".equals(status)) {
                depositSum += booking.getDepositAmount();
            }
        }
        tvDepositCollected.setText(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(depositSum));
        tvPendingBadge.setText(pendingCount > 0 ? pendingCount + " chờ cọc" : "0 chờ cọc");
        tvPendingBadge.setVisibility(pendingCount > 0 ? View.VISIBLE : View.GONE);
    }

    private boolean matchesStatus(Booking booking) {
        if ("ALL".equals(selectedStatus)) {
            return true;
        }
        return selectedStatus.equals(safeStatus(booking));
    }

    private boolean matchesDate(Booking booking) {
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            return true;
        }
        return selectedDate.equals(safeString(booking.getBookingDate()));
    }

    private boolean matchesQuery(Booking booking) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String source = buildSearchSource(booking);
        return source.contains(query.toLowerCase(Locale.US));
    }

    private String buildSearchSource(Booking booking) {
        StringBuilder builder = new StringBuilder();
        builder.append("#").append(booking.getId()).append(' ');
        builder.append(safeString(booking.getFieldName())).append(' ');
        builder.append(safeString(booking.getNote())).append(' ');
        builder.append(safeString(booking.getBookingDate())).append(' ');
        builder.append(safeString(booking.getStartTime())).append(' ');
        builder.append(safeString(booking.getEndTime())).append(' ');
        builder.append(safeStatus(booking)).append(' ');
        builder.append(safeString(String.valueOf(booking.getFieldId())));
        return builder.toString().toLowerCase(Locale.US);
    }

    // 6. Cấu hình Dialog chọn ngày với Context là requireContext()
    private void openDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    applyFilters();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void clearFilters() {
        selectedDate = null;
        selectedStatus = "ALL";
        query = "";
        if (etSearchBookings != null) {
            etSearchBookings.setText("");
        }
        updateTabState();
        applyFilters();
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

    private String safeStatus(Booking booking) {
        return booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }
}