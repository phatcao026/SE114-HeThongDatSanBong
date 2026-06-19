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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking b = bookings.get(position);
            holder.tvFieldName.setText(b.getFieldName());
            holder.tvTime.setText(String.format("%s | %s - %s", b.getBookingDate(), b.getStartTime(), b.getEndTime()));
            holder.tvPrice.setText(String.format(java.util.Locale.getDefault(), "%,.0fđ", (double)b.getTotalPrice()));
            holder.tvStatus.setText(b.getStatus());

            if ("PENDING".equalsIgnoreCase(b.getStatus())) {
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7")));
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#92400E"));
            } else if ("CANCELLED".equalsIgnoreCase(b.getStatus())) {
                holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF2F2")));
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            }
        }

        @Override
        public int getItemCount() { return bookings.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFieldName, tvTime, tvPrice, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvFieldName = v.findViewById(R.id.tvBookingFieldName);
                tvTime = v.findViewById(R.id.tvBookingTime);
                tvPrice = v.findViewById(R.id.tvBookingPrice);
                tvStatus = v.findViewById(R.id.tvBookingStatus);
            }
        }
    }
}
