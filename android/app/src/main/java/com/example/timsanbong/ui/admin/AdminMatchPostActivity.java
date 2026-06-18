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
import com.example.timsanbong.data.model.MatchPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMatchPostActivity extends AppCompatActivity {

    private AdminMatchPostAdapter adapter;
    private final List<MatchPost> matchPosts = new ArrayList<>();
    private TextView tvPostCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_match_posts);

        tvPostCount = findViewById(R.id.tvPostCount);
        RecyclerView rv = findViewById(R.id.rvAdminMatchPosts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminMatchPostAdapter(matchPosts);
        rv.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchMatchPosts();

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_AUDIT);
        navBarManager.setup();
    }

    private void fetchMatchPosts() {
        ApiClient.getService(this).getAdminMatchPosts().enqueue(new Callback<List<MatchPost>>() {
            @Override
            public void onResponse(@NonNull Call<List<MatchPost>> call, @NonNull Response<List<MatchPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    matchPosts.clear();
                    matchPosts.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvPostCount.setText(String.format(Locale.getDefault(), "%d kèo", matchPosts.size()));
                } else {
                    Toast.makeText(AdminMatchPostActivity.this, "Không thể tải danh sách kèo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MatchPost>> call, @NonNull Throwable t) {
                Toast.makeText(AdminMatchPostActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class AdminMatchPostAdapter extends RecyclerView.Adapter<AdminMatchPostAdapter.ViewHolder> {
        private final List<MatchPost> posts;
        AdminMatchPostAdapter(List<MatchPost> posts) { this.posts = posts; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_match_post, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MatchPost p = posts.get(position);
            holder.tvMatchPostTitle.setText(String.format("[%s] %s", p.getTypeLabel(), p.getTeamName()));
            holder.tvMatchPostDetails.setText(String.format("%s | %s | %s", p.getField(), p.getDate(), p.getTime()));
            holder.tvMatchPostMessage.setText(p.getMessage());
            holder.tvMatchPostStatus.setText(p.getStatus());
            holder.tvMatchPostRequests.setText(String.format(Locale.getDefault(), "%s yêu cầu", p.getMembersSlot()));
            holder.tvMatchPostDate.setText(p.getTimeAgo());

            holder.itemView.setOnLongClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                        .setTitle("Xóa bài đăng")
                        .setMessage("Bạn có chắc chắn muốn xóa bài đăng này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> deletePost(p.getId(), v))
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            });
        }

        private void deletePost(long id, View v) {
            ApiClient.getService(v.getContext()).deleteMatchPost(id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(v.getContext(), "Đã xóa bài đăng", Toast.LENGTH_SHORT).show();
                        if (v.getContext() instanceof AdminMatchPostActivity) {
                            ((AdminMatchPostActivity) v.getContext()).fetchMatchPosts();
                        }
                    } else {
                        Toast.makeText(v.getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(v.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() { return posts.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMatchPostTitle, tvMatchPostDetails, tvMatchPostMessage, tvMatchPostStatus, tvMatchPostRequests, tvMatchPostDate;
            ViewHolder(View v) {
                super(v);
                tvMatchPostTitle = v.findViewById(R.id.tvMatchPostTitle);
                tvMatchPostDetails = v.findViewById(R.id.tvMatchPostDetails);
                tvMatchPostMessage = v.findViewById(R.id.tvMatchPostMessage);
                tvMatchPostStatus = v.findViewById(R.id.tvMatchPostStatus);
                tvMatchPostRequests = v.findViewById(R.id.tvMatchPostRequests);
                tvMatchPostDate = v.findViewById(R.id.tvMatchPostDate);
            }
        }
    }
}
