package com.example.timsanbong.ui.customer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity implements BookingAdapter.OnBookingActionListener {

    private RecyclerView rvBookings;
    private NavBarManager navBarManager;
    private BookingViewModel bookingViewModel;
    private android.widget.ProgressBar pbLoading;
    private android.widget.TextView tvEmptyState;
    private android.widget.TextView tvErrorState;
    private BookingAdapter bookingAdapter;
    private List<Booking> allBookings = new ArrayList<>();
    private TextView tvTabUpcoming, tvTabPast;
    private View indicatorUpcoming, indicatorPast;
    private LinearLayout layoutRefresh;
    private android.widget.TextView tvRefreshLabel;
    private boolean isRefreshing;
    private float pullStartY = -1f;
    private int pullThresholdPx;
    private boolean showUpcoming = true;
    private long pendingCancelBookingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_my_bookings);

        rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);
        tvTabUpcoming = findViewById(R.id.tvTabUpcoming);
        tvTabPast = findViewById(R.id.tvTabPast);
        indicatorUpcoming = findViewById(R.id.indicatorUpcoming);
        indicatorPast = findViewById(R.id.indicatorPast);
        layoutRefresh = findViewById(R.id.layoutRefresh);
        tvRefreshLabel = findViewById(R.id.tvRefreshLabel);
        pullThresholdPx = getResources().getDimensionPixelSize(R.dimen.pull_refresh_threshold);

        bookingAdapter = new BookingAdapter(new ArrayList<>(), this);
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

        setupTabs();
        setupPullToRefresh();

        bookingViewModel.loadMyBookings();
    }

    private void setupTabs() {
        findViewById(R.id.tabUpcoming).setOnClickListener(v -> selectTab(true));
        findViewById(R.id.tabPast).setOnClickListener(v -> selectTab(false));
    }

    private void selectTab(boolean upcoming) {
        showUpcoming = upcoming;
        tvTabUpcoming.setTextColor(getColor(upcoming ? R.color.primary_dark : R.color.text_secondary));
        tvTabPast.setTextColor(getColor(upcoming ? R.color.text_secondary : R.color.primary_dark));
        indicatorUpcoming.setBackgroundColor(getColor(upcoming ? R.color.primary : android.R.color.transparent));
        indicatorPast.setBackgroundColor(getColor(upcoming ? android.R.color.transparent : R.color.primary));
        applyFilterAndSort();
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
        List<Booking> upcoming = new ArrayList<>();
        List<Booking> past = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (isUpcoming(booking)) {
                upcoming.add(booking);
            } else {
                past.add(booking);
            }
        }
        // Upcoming: soonest first; past: most recent first
        Collections.sort(upcoming, (a, b) -> Long.compare(getBookingDateMillis(a), getBookingDateMillis(b)));
        Collections.sort(past, (a, b) -> Long.compare(getBookingDateMillis(b), getBookingDateMillis(a)));

        tvTabUpcoming.setText(getString(R.string.tab_upcoming, upcoming.size()));
        tvTabPast.setText(getString(R.string.tab_past, past.size()));

        List<Booking> visible = showUpcoming ? upcoming : past;
        bookingAdapter.updateBookings(visible);
        if (visible.isEmpty()) {
            showEmptyState();
        } else {
            showContentState();
        }
    }

    private boolean isUpcoming(Booking booking) {
        String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
        boolean active = "PENDING".equals(status) || "DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status);
        if (!active) {
            return false;
        }
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return getBookingDateMillis(booking) >= today.getTimeInMillis();
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

    // ── BookingAdapter.OnBookingActionListener ──────────────

    @Override
    public void onCancel(long bookingId) {
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

    @Override
    public void onQrCheckin(Booking booking) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.qr_dialog_title)
                .setMessage(getString(R.string.qr_dialog_message, booking.getId()))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onDirections(Booking booking) {
        String query = booking.getFieldName() != null ? booking.getFieldName() : "";
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=" + Uri.encode(query)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
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
