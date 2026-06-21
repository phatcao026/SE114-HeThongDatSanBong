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
import com.example.timsanbong.ui.owner.BookingCardAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// 1. Chuyển đổi từ AppCompatActivity sang Fragment
public class OwnerBookingFragment extends Fragment {
    private OwnerBookingViewModel viewModel;
    private BookingCardAdapter adapter;
    private final List<Booking> allBookings = new ArrayList<>();
    private final List<Booking> visibleBookings = new ArrayList<>();
    private EditText etSearchBookings;
    private TextView tvTotalBookings;
    private TextView tvDepositCollected;
    private TextView tvPendingBadge;
    private TextView tvTabAllCount, tvTabPendingCount, tvTabConfirmedCount, tvTabCompletedCount, tvTabCancelledCount;
    private TextView tvRevenue;
    private View emptyState;
    private final TextView tvEmptyBookings = null;
    private View btnClearFilters;
    private String selectedDate;
    private String selectedStatus = "ALL";
    private String query = "";
    private java.util.Set<String> filterStatuses = new java.util.HashSet<>();
    private String filterFieldName = "";

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

        tvTabAllCount = view.findViewById(R.id.tvTabAllCount);
        tvTabPendingCount = view.findViewById(R.id.tvTabPendingCount);
        tvTabConfirmedCount = view.findViewById(R.id.tvTabConfirmedCount);
        tvTabCompletedCount = view.findViewById(R.id.tvTabCompletedCount);
        tvTabCancelledCount = view.findViewById(R.id.tvTabCancelledCount);

        tvPendingBadge = view.findViewById(R.id.tvPendingBadge);
        emptyState = view.findViewById(R.id.llEmptyState);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvBookings = view.findViewById(R.id.rvBookingList);
        adapter = new BookingCardAdapter(new BookingCardAdapter.Listener() {
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

            @Override
            public void onConfirm(Booking booking) {
                confirmAction(
                        "Xác nhận booking",
                        "Xác nhận đơn #" + booking.getId() + "?",
                        () -> viewModel.confirmBooking(booking.getId()));
            }

            @Override
            public void onCancel(Booking booking) {
                confirmAction(
                        "Hủy booking",
                        "Hủy đơn #" + booking.getId() + "?",
                        () -> viewModel.cancelBooking(booking.getId()));
            }

            @Override
            public void onComplete(Booking booking) {
                confirmAction(
                        "Hoàn tất booking",
                        "Xác nhận hoàn tất đơn #" + booking.getId() + "?",
                        () -> viewModel.completeBooking(booking.getId()));
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
        View btnFilterOptions = view.findViewById(R.id.btnFilterOptions);
        if (btnFilterOptions != null) {
            btnFilterOptions.setOnClickListener(v -> openFilterDialog());
        }
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
                // QUAN TRỌNG: Xóa filter nâng cao để Tab hoạt động chính xác
                if (filterStatuses != null) filterStatuses.clear();

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
            View tabView = getView().findViewById(viewId);

            if (tabView instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout tab = (android.widget.LinearLayout) tabView;
                tab.setSelected(selected);

                // Đổi background dựa trên trạng thái chọn
                tab.setBackgroundResource(selected ? R.drawable.bg_tab_active : R.drawable.bg_tab_inactive);

                // Đổi màu chữ và kiểu chữ cho các TextView bên trong Tab
                for (int i = 0; i < tab.getChildCount(); i++) {
                    View child = tab.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView tv = (TextView) child;
                        tv.setTextColor(selected ? android.graphics.Color.WHITE :
                                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.slate_500));

                        if (i == 0) { // Label (All, Pending...)
                            tv.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                        } else { // Count badge
                            if (selected) {
                                tv.setBackgroundColor(android.graphics.Color.parseColor("#40FFFFFF"));
                            } else {
                                tv.setBackgroundResource(R.color.slate_200);
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Đồng bộ hóa bộ lắng nghe bằng 'getViewLifecycleOwner()' thay vì 'this'
    private void setupObservers() {
        View loadingBar = getActivity() != null ? getActivity().findViewById(R.id.ownerLoadingBar) : null;
        View fragmentRootView = getView();

        // CHỈ DÙNG 1 OBSERVER DUY NHẤT CHO LOADING
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = (isLoading != null && isLoading);

            // 1. Xử lý vòng xoay của Activity
            if (loadingBar != null) {
                loadingBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }

            // 2. Xử lý làm mờ Fragment
            if (fragmentRootView != null) {
                fragmentRootView.setAlpha(loading ? 0.4f : 1.0f);
                fragmentRootView.setEnabled(!loading);
            }

            // 3. Cập nhật trạng thái cho Adapter (nếu có đơn đang xử lý)
            if (adapter != null) {
                long busyId = viewModel.getBusyBookingId().getValue() == null ? -1L : viewModel.getBusyBookingId().getValue();
                adapter.setActionState(loading, busyId);
            }
        });

        // Lắng nghe danh sách booking từ ViewModel
        viewModel.getBookings().observe(getViewLifecycleOwner(), list -> {
            allBookings.clear();
            if (list != null) {
                allBookings.addAll(list);
            }
            updateSummary();
            applyFilters();
        });

        // Lắng nghe thông báo (Toast)
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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

        int all = allBookings.size();
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        int cancelled = 0;
        double depositSum = 0;

        for (Booking booking : allBookings) {
            String status = safeStatus(booking);
            switch (status) {
                case "PENDING": pending++; break;
                case "CONFIRMED": confirmed++; break;
                case "COMPLETED": completed++; break;
                case "CANCELLED": cancelled++; break;
                case "DEPOSIT_PAID": confirmed++; break; // Hoặc xử lý riêng tùy logic backend
            }

            if (!"PENDING".equals(status) && !"CANCELLED".equals(status)) {
                depositSum += booking.getDepositAmount();
            }
        }

        if (tvTabAllCount != null) tvTabAllCount.setText(String.valueOf(all));
        if (tvTabPendingCount != null) tvTabPendingCount.setText(String.valueOf(pending));
        if (tvTabConfirmedCount != null) tvTabConfirmedCount.setText(String.valueOf(confirmed));
        if (tvTabCompletedCount != null) tvTabCompletedCount.setText(String.valueOf(completed));
        if (tvTabCancelledCount != null) tvTabCancelledCount.setText(String.valueOf(cancelled));

        if (tvPendingBadge != null) {
            tvPendingBadge.setText(pending + " chờ cọc");
            tvPendingBadge.setVisibility(pending > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private boolean matchesStatus(Booking booking) {
        String status = safeStatus(booking);
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            return filterStatuses.contains(status);
        }
        if ("ALL".equals(selectedStatus)) {
            return true;
        }
        return selectedStatus.equals(status);
    }

    private boolean matchesDate(Booking booking) {
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            return true;
        }
        String bDate = safeString(booking.getBookingDate());
        // Chỉ lấy phần yyyy-MM-dd nếu bDate có chứa giờ (ISO format)
        if (bDate.contains("T")) {
            bDate = bDate.split("T")[0];
        }
        return selectedDate.equals(bDate);
    }

    private boolean matchesQuery(Booking booking) {
        if (query == null || query.trim().isEmpty()) {
            // Still apply field filter if present
            if (filterFieldName != null && !filterFieldName.trim().isEmpty()) {
                return safeString(booking.getFieldName()).toLowerCase(Locale.US).contains(filterFieldName.toLowerCase(Locale.US));
            }
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
        filterStatuses.clear();
        filterFieldName = "";
        query = "";
        if (etSearchBookings != null) {
            etSearchBookings.setText("");
        }
        updateTabState();
        applyFilters();
    }

    private void openFilterDialog() {
        // Multi-select statuses + field name input
        final String[] statusOptions = new String[]{"PENDING", "DEPOSIT_PAID", "CONFIRMED", "COMPLETED", "CANCELLED"};
        final boolean[] checked = new boolean[statusOptions.length];
        for (int i = 0; i < statusOptions.length; i++) {
            checked[i] = filterStatuses.contains(statusOptions[i]);
        }

        android.widget.LinearLayout container = new android.widget.LinearLayout(requireContext());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.EditText etField = new android.widget.EditText(requireContext());
        etField.setHint("Tên sân (tùy chọn)");
        etField.setText(filterFieldName == null ? "" : filterFieldName);
        container.setPadding(40, 10, 40, 10);
        container.addView(etField);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Bộ lọc nâng cao")
                .setMultiChoiceItems(statusOptions, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setView(container)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    filterStatuses.clear();
                    for (int i = 0; i < statusOptions.length; i++) {
                        if (checked[i]) filterStatuses.add(statusOptions[i]);
                    }
                    filterFieldName = etField.getText() == null ? "" : etField.getText().toString().trim();
                    applyFilters();
                })
                .show();
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
