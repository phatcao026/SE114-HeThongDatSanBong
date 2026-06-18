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
import com.example.timsanbong.data.model.ReviewResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReviewActivity extends AppCompatActivity {

    private AdminReviewAdapter adapter;
    private final List<ReviewResponse> reviews = new ArrayList<>();
    private TextView tvReviewCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        tvReviewCount = findViewById(R.id.tvReviewCount);
        RecyclerView rv = findViewById(R.id.rvAdminReviews);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminReviewAdapter(reviews);
        rv.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchReviews();

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_AUDIT);
        navBarManager.setup();
    }

    private void fetchReviews() {
        ApiClient.getService(this).getAdminReviews().enqueue(new Callback<List<ReviewResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviews.clear();
                    reviews.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvReviewCount.setText(String.format(Locale.getDefault(), "%d đánh giá", reviews.size()));
                } else {
                    Toast.makeText(AdminReviewActivity.this, "Không thể tải danh sách đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                Toast.makeText(AdminReviewActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {
        private final List<ReviewResponse> reviews;
        AdminReviewAdapter(List<ReviewResponse> reviews) { this.reviews = reviews; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_review, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ReviewResponse r = reviews.get(position);
            holder.tvReviewerName.setText(r.getReviewerName());
            holder.tvRevieweeName.setText(String.format("Đánh giá: %s", r.getRevieweeName()));
            holder.tvReviewReason.setText(r.getReason());
            holder.tvReviewStatus.setText(r.getStatus());
            holder.tvReviewDate.setText(r.getCreatedAt());
        }

        @Override
        public int getItemCount() { return reviews.size(); }

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
