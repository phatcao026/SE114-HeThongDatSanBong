package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private NavBarManager navBarManager;
    private BookingViewModel bookingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_BOOKINGS);
        navBarManager.setup();

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        bookingViewModel.bookingsState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                // Show loading if desired
            } else if (resource.status == Resource.Status.SUCCESS) {
                rvBookings.setAdapter(new BookingAdapter(resource.data, bookingId -> {
                    bookingViewModel.cancelBooking(bookingId);
                }));
            } else {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        bookingViewModel.cancelState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        bookingViewModel.loadMyBookings();
    }
}
