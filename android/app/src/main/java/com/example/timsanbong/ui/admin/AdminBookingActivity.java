package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Booking;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookingActivity extends AppCompatActivity {

    private AdminBookingAdapter adapter;
    private List<Booking> allBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_booking);

        RecyclerView rv = findViewById(R.id.rvAdminBookings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        allBookings = new ArrayList<>();
        adapter = new AdminBookingAdapter(allBookings);
        rv.setAdapter(adapter);

        fetchBookings();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        // We use ITEM_AUDIT or something else since we don't have a specific nav item for bookings yet
        // or we can just leave it as is.
        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_AUDIT);
        navBarManager.setup();
    }

    private void fetchBookings() {
        ApiClient.getService(this).getAdminBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBookings.clear();
                    allBookings.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    ((TextView) findViewById(R.id.tvBookingCount)).setText(allBookings.size() + " lượt đặt");
                } else {
                    Toast.makeText(AdminBookingActivity.this, "Không thể tải danh sách đặt sân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Toast.makeText(AdminBookingActivity.this, "Lỗi kết nối - Sử dụng dữ liệu demo", Toast.LENGTH_SHORT).show();
                setupDemoBookings();
            }
        });
    }

    private void setupDemoBookings() {
        allBookings.clear();
        allBookings.add(new Booking(1, null, null, "2024-06-12", "18:00", "19:00", 250000, "CONFIRMED"));
        allBookings.add(new Booking(2, null, null, "2024-06-12", "20:00", "21:00", 300000, "PENDING"));
        adapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.tvBookingCount)).setText(allBookings.size() + " lượt đặt");
    }

    static class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {
        private final List<Booking> bookings;
        AdminBookingAdapter(List<Booking> bookings) { this.bookings = bookings; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reusing item_admin_transaction style or similar if possible, but let's just create a simple view
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking b = bookings.get(position);
            holder.text1.setText(b.getFieldName() + " - " + b.getStatus());
            holder.text2.setText(b.getBookingDate() + " | " + b.getStartTime() + " - " + b.getEndTime());
        }

        @Override
        public int getItemCount() { return bookings.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
