package com.example.fieldbooking.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fieldbooking.R;
import com.example.fieldbooking.adapter.BookingAdapter;
import com.example.fieldbooking.model.Booking;
import com.example.fieldbooking.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        loadBookings();
    }

    private void loadBookings() {
        ApiClient.getService(this).getMyBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rvBookings.setAdapter(new BookingAdapter(response.body(), bookingId -> cancelBooking(bookingId)));
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Toast.makeText(MyBookingsActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelBooking(long bookingId) {
        ApiClient.getService(this).cancelBooking(bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyBookingsActivity.this, R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
                    loadBookings();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MyBookingsActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
