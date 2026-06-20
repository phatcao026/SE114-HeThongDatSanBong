package com.example.timsanbong.ui.owner;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.OwnerDashboardStats;
import com.example.timsanbong.utils.PushNotificationManager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OwnerDashboardActivity extends AppCompatActivity {
    private OwnerFieldViewModel fieldViewModel;
    private OwnerBookingViewModel bookingViewModel;
    private OwnerBookingAdapter bookingAdapter;
    private TextView tvTotalFields;
    private TextView tvPendingBookings;
    private TextView tvConfirmedBookings;
    private TextView tvCompletedRevenue;
    private TextView tvEmptyRecentBookings;
    private List<Field> currentFields = Collections.emptyList();
    private List<Booking> currentBookings = Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);
        PushNotificationManager.prepareForAuthenticatedUser(this);

        fieldViewModel = new ViewModelProvider(this).get(OwnerFieldViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(OwnerBookingViewModel.class);

        bindViews();
        setupActions();
        setupRecentBookings();
        setupObservers();

        new OwnerNavBarManager(this, OwnerNavBarManager.ITEM_DASHBOARD).setup();
        fieldViewModel.loadFields();
        bookingViewModel.loadBookings();
    }

    private void bindViews() {
        tvTotalFields = findViewById(R.id.tvOwnerTotalFields);
        tvPendingBookings = findViewById(R.id.tvOwnerPendingBookings);
        tvConfirmedBookings = findViewById(R.id.tvOwnerConfirmedBookings);
        tvCompletedRevenue = findViewById(R.id.tvOwnerCompletedRevenue);
        tvEmptyRecentBookings = findViewById(R.id.tvOwnerEmptyRecentBookings);
    }

    private void setupActions() {
        View createField = findViewById(R.id.btn_create_field);
        View viewBookings = findViewById(R.id.btn_view_bookings);
        View manageFields = findViewById(R.id.btn_manage_fields);
        View allDeposits = findViewById(R.id.btn_all_deposits);
        View quickFields = findViewById(R.id.qa_fields);
        View quickBookings = findViewById(R.id.qa_bookings);

        View.OnClickListener fieldsListener = v -> startActivity(
                new Intent(this, OwnerFieldManagementActivity.class));
        View.OnClickListener bookingsListener = v -> startActivity(
                new Intent(this, OwnerBookingActivity.class));

        if (createField != null) createField.setOnClickListener(fieldsListener);
        if (manageFields != null) manageFields.setOnClickListener(fieldsListener);
        if (quickFields != null) quickFields.setOnClickListener(fieldsListener);
        if (viewBookings != null) viewBookings.setOnClickListener(bookingsListener);
        if (allDeposits != null) allDeposits.setOnClickListener(bookingsListener);
        if (quickBookings != null) quickBookings.setOnClickListener(bookingsListener);
    }

    private void setupRecentBookings() {
        RecyclerView rvRecentBookings = findViewById(R.id.rvOwnerRecentBookings);
        bookingAdapter = new OwnerBookingAdapter(new OwnerBookingAdapter.Listener() {
            @Override
            public void onConfirmBooking(Booking booking) {
                bookingViewModel.confirmBooking(booking.getId());
            }

            @Override
            public void onCompleteBooking(Booking booking) {
                bookingViewModel.completeBooking(booking.getId());
            }

            @Override
            public void onCancelBooking(Booking booking) {
                bookingViewModel.cancelBooking(booking.getId());
            }
        });
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));
        rvRecentBookings.setAdapter(bookingAdapter);
    }

    private void setupObservers() {
        fieldViewModel.getFields().observe(this, fields -> {
            currentFields = fields == null ? Collections.emptyList() : fields;
            renderStats();
        });
        bookingViewModel.getBookings().observe(this, bookings -> {
            currentBookings = bookings == null ? Collections.emptyList() : bookings;
            bookingAdapter.submitList(currentBookings);
            tvEmptyRecentBookings.setVisibility(currentBookings.isEmpty() ? View.VISIBLE : View.GONE);
            renderStats();
        });
        fieldViewModel.getMessage().observe(this, this::showMessage);
        bookingViewModel.getMessage().observe(this, this::showMessage);
    }

    private void renderStats() {
        OwnerDashboardStats stats = OwnerDashboardStats.from(currentFields, currentBookings);
        tvTotalFields.setText(String.valueOf(stats.getTotalFields()));
        tvPendingBookings.setText(String.valueOf(stats.getPendingBookings()));
        tvConfirmedBookings.setText(String.valueOf(stats.getConfirmedBookings()));
        tvCompletedRevenue.setText(String.format(Locale.US, "%,.0f d", stats.getCompletedRevenue()));
    }

    private void showMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
