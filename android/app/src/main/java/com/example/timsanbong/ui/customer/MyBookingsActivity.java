package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private NavBarManager navBarManager;
    private BookingViewModel bookingViewModel;
    private android.widget.ProgressBar pbLoading;
    private android.widget.TextView tvEmptyState;
    private android.widget.TextView tvErrorState;
    private BookingAdapter bookingAdapter;
    private List<Booking> allBookings = new ArrayList<>();
    private ChipGroup cgStatusFilter;
    private LinearLayout layoutRefresh;
    private android.widget.TextView tvRefreshLabel;
    private boolean isRefreshing;
    private float pullStartY = -1f;
    private int pullThresholdPx;
    private String currentStatusFilter = "ALL";
    private long pendingCancelBookingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);
        cgStatusFilter = findViewById(R.id.cgStatusFilter);
        layoutRefresh = findViewById(R.id.layoutRefresh);
        tvRefreshLabel = findViewById(R.id.tvRefreshLabel);
        pullThresholdPx = getResources().getDimensionPixelSize(R.dimen.pull_refresh_threshold);

        bookingAdapter = new BookingAdapter(new ArrayList<>(), this::showCancelConfirmDialog);
        rvBookings.setAdapter(bookingAdapter);

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_BOOKINGS);
        navBarManager.setup();

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        bookingViewModel.bookingsState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                if (isRefreshing) {
                    showRefreshingState();
                } else {
                    showLoadingState();
                }
            } else if (resource.status == Resource.Status.SUCCESS) {
                setRefreshing(false);
                allBookings = resource.data == null ? new ArrayList<>() : new ArrayList<>(resource.data);
                applyFilterAndSort();
            } else {
                String message = getSafeMessage(resource.message);
                setRefreshing(false);
                if (allBookings.isEmpty()) {
                    showErrorState(message);
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bookingViewModel.cancelState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                if (pendingCancelBookingId != -1) {
                    removeBookingById(pendingCancelBookingId);
                    pendingCancelBookingId = -1;
                }
                Toast.makeText(this, R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, getSafeMessage(resource.message), Toast.LENGTH_SHORT).show();
            }
        });

        setupFilterChips();
        setupPullToRefresh();

        bookingViewModel.loadMyBookings();
    }

    private void setupFilterChips() {
        cgStatusFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPending) {
                currentStatusFilter = "PENDING";
            } else if (checkedId == R.id.chipConfirmed) {
                currentStatusFilter = "CONFIRMED";
            } else if (checkedId == R.id.chipCancelled) {
                currentStatusFilter = "CANCELLED";
            } else {
                currentStatusFilter = "ALL";
            }
            applyFilterAndSort();
        });
    }

    private void setupPullToRefresh() {
        rvBookings.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pullStartY = !rvBookings.canScrollVertically(-1) ? event.getY() : -1f;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (pullStartY >= 0 && !isRefreshing && !rvBookings.canScrollVertically(-1)) {
                    float delta = event.getY() - pullStartY;
                    if (delta > pullThresholdPx) {
                        triggerRefresh();
                        pullStartY = -1f;
                        return true;
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                pullStartY = -1f;
            }
            return false;
        });
    }

    private void triggerRefresh() {
        setRefreshing(true);
        bookingViewModel.loadMyBookings();
    }

    private void setRefreshing(boolean refreshing) {
        isRefreshing = refreshing;
        layoutRefresh.setVisibility(refreshing ? View.VISIBLE : View.GONE);
        tvRefreshLabel.setText(refreshing ? R.string.refreshing : R.string.pull_to_refresh);
    }

    private void showRefreshingState() {
        layoutRefresh.setVisibility(View.VISIBLE);
        tvRefreshLabel.setText(R.string.refreshing);
    }

    private void applyFilterAndSort() {
        List<Booking> filtered = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (matchesFilter(booking)) {
                filtered.add(booking);
            }
        }
        Collections.sort(filtered, (a, b) -> Long.compare(getBookingDateMillis(b), getBookingDateMillis(a)));
        bookingAdapter.updateBookings(filtered);
        if (filtered.isEmpty()) {
            showEmptyState();
        } else {
            showContentState();
        }
    }

    private boolean matchesFilter(Booking booking) {
        if ("ALL".equals(currentStatusFilter)) {
            return true;
        }
        String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
        if ("CANCELLED".equals(currentStatusFilter)) {
            return "CANCELLED".equals(status) || "CANCELED".equals(status);
        }
        return currentStatusFilter.equals(status);
    }

    private long getBookingDateMillis(Booking booking) {
        if (booking == null || booking.getBookingDate() == null) {
            return 0;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.util.Date parsed = format.parse(booking.getBookingDate());
            return parsed != null ? parsed.getTime() : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    private void showCancelConfirmDialog(long bookingId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancel_booking_title)
                .setMessage(R.string.cancel_booking_message)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_confirm_cancel, (dialog, which) -> {
                    pendingCancelBookingId = bookingId;
                    bookingViewModel.cancelBooking(bookingId);
                })
                .show();
    }

    private void removeBookingById(long bookingId) {
        List<Booking> updated = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (booking.getId() != bookingId) {
                updated.add(booking);
            }
        }
        allBookings = updated;
        applyFilterAndSort();
    }

    private void showLoadingState() {
        pbLoading.setVisibility(android.view.View.VISIBLE);
        rvBookings.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
        layoutRefresh.setVisibility(View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(android.view.View.GONE);
        rvBookings.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showEmptyState() {
        pbLoading.setVisibility(android.view.View.GONE);
        rvBookings.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setVisibility(android.view.View.GONE);
        layoutRefresh.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(android.view.View.GONE);
        rvBookings.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setText(message);
        layoutRefresh.setVisibility(View.GONE);
    }

    private String getSafeMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return getString(R.string.error_unknown);
        }
        return message;
    }
}
