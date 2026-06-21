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
import com.example.timsanbong.data.model.FieldReviewResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReviewActivity extends AppCompatActivity {

    private AdminReviewAdapter adapter;
    private final List<FieldReviewResponse> displayItems = new ArrayList<>();
    private TextView tvReviewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        initViews();
        fetchData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_AUDIT);
        navBarManager.setup();
    }

    private void initViews() {
        tvReviewCount = findViewById(R.id.tvReviewCount);
        RecyclerView rv = findViewById(R.id.rvAdminReviews);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminReviewAdapter(displayItems);
        rv.setAdapter(adapter);
    }

    private void fetchData() {
        ApiClient.getService(this).getAdminFieldReviews().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<FieldReviewResponse>> call, @NonNull Response<List<FieldReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayItems.clear();
                    displayItems.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvReviewCount.setText(String.format(Locale.getDefault(), "%d đánh giá", displayItems.size()));
                } else {
                    Toast.makeText(AdminReviewActivity.this, "Không thể tải danh sách đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FieldReviewResponse>> call, @NonNull Throwable t) {
                Toast.makeText(AdminReviewActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {
        private final List<FieldReviewResponse> items;
        
        AdminReviewAdapter(List<FieldReviewResponse> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FieldReviewResponse r = items.get(position);
            holder.tvReviewerName.setText(r.getReviewerName());
            holder.tvRevieweeName.setText(String.format(Locale.getDefault(), "Sân ID: %d", r.getFieldId()));
            
            String comment = r.getComment();
            if (comment != null && !comment.trim().isEmpty()) {
                holder.tvReviewReason.setVisibility(View.VISIBLE);
                holder.tvReviewReason.setText(comment);
            } else {
                holder.tvReviewReason.setVisibility(View.GONE);
            }

            holder.tvReviewStatus.setText(String.format(Locale.getDefault(), "%.1f ⭐", r.getRating() != null ? r.getRating().floatValue() : 0f));
            holder.tvReviewDate.setText(formatDateTime(r.getCreatedAt()));
        }

        private String formatDateTime(String isoString) {
            if (isoString == null || isoString.isEmpty()) return "";
            try {
                // Handle formats like 2026-06-21T10:06:27.916395
                String cleanIso = isoString;
                if (isoString.contains(".")) {
                    int dotIndex = isoString.lastIndexOf(".");
                    cleanIso = isoString.substring(0, dotIndex);
                }
                
                SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = sdfInput.parse(cleanIso);
                
                SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return date != null ? sdfOutput.format(date) : isoString;
            } catch (Exception e) {
                return isoString;
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvReviewerName, tvRevieweeName, tvReviewReason, tvReviewStatus, tvReviewDate;
            ViewHolder(View v) {
                super(v);
                tvReviewerName = v.findViewById(R.id.tvReviewerName);
                tvRevieweeName = v.findViewById(R.id.tvRevieweeName);
                tvReviewReason = v.findViewById(R.id.tvReviewReason);
                tvReviewStatus = v.findViewById(R.id.tvReviewStatus);
                tvReviewDate = v.findViewById(R.id.tvReviewDate);
            }
        }
    }
}
