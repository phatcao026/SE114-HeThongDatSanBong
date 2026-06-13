package com.example.timsanbong.ui.owner;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;

public class OwnerBookingActivity extends AppCompatActivity {
    private OwnerBookingViewModel viewModel;
    private OwnerBookingAdapter adapter;
    private TextView tvEmptyBookings;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_booking);

        viewModel = new ViewModelProvider(this).get(OwnerBookingViewModel.class);
        tvEmptyBookings = findViewById(R.id.tvOwnerEmptyBookings);
        emptyState = findViewById(R.id.ll_empty_state);

        setupRecyclerView();
        setupObservers();
        new OwnerNavBarManager(this, OwnerNavBarManager.ITEM_BOOKINGS).setup();

        viewModel.loadBookings();
    }

    private void setupRecyclerView() {
        RecyclerView rvBookings = findViewById(R.id.rvOwnerBookings);
        adapter = new OwnerBookingAdapter(new OwnerBookingAdapter.Listener() {
            @Override
            public void onConfirmBooking(Booking booking) {
                viewModel.confirmBooking(booking.getId());
            }

            @Override
            public void onCompleteBooking(Booking booking) {
                viewModel.completeBooking(booking.getId());
            }

            @Override
            public void onCancelBooking(Booking booking) {
                viewModel.cancelBooking(booking.getId());
            }
        });
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getBookings().observe(this, bookings -> {
            adapter.submitList(bookings);
            boolean empty = bookings == null || bookings.isEmpty();
            tvEmptyBookings.setVisibility(empty ? View.VISIBLE : View.GONE);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
