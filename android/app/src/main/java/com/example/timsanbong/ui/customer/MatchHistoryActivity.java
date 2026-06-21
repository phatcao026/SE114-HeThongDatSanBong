package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.ReviewRequest;
import com.example.timsanbong.data.repository.MatchRepository;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchHistoryActivity extends AppCompatActivity {

    private RecyclerView rvMatchHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MatchAdapter adapter;
    private SessionManager sessionManager;
    private final MatchRepository matchRepository = new MatchRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_match_history);

        rvMatchHistory = findViewById(R.id.rvMatchHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        sessionManager = new SessionManager(this);
        rvMatchHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {}
            @Override
            public void onChat(MatchPost match) {}
            @Override
            public void onCardClick(MatchPost match) {}
            @Override
            public void onRate(MatchPost match) {
                showReviewDialog(match);
            }
        });
        adapter.setHistoryMode(true);
        rvMatchHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);
        java.util.Set<Long> acceptedIds = matchRepository.getAcceptedMatchIds(this);
        
        // We need to fetch both "my posts" and "posts I accepted".
        // For now, let's fetch ALL posts and filter by (created by me OR id in acceptedIds)
        ApiClient.getService(this).getMatchPosts(null).enqueue(new Callback<List<MatchPost>>() {
            @Override
            public void onResponse(Call<List<MatchPost>> call, Response<List<MatchPost>> response) {
                if (!MatchHistoryActivity.this.isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<MatchPost> history = new ArrayList<>();
                    long myUserId = sessionManager.getUserId();
                    for (MatchPost p : response.body()) {
                        boolean isMine = p.getUserId() == myUserId;
                        boolean isAcceptedByMe = acceptedIds.contains(p.getId());
                        
                        if (isMine || isAcceptedByMe) {
                            // Ensure it shows the Rate button
                            forceHistoryStatus(p);
                            history.add(p);
                        }
                    }
                    adapter.updateMatches(history);
                    tvEmpty.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    android.util.Log.e("MatchHistory", "Failed to load: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MatchPost>> call, Throwable t) {
                if (!MatchHistoryActivity.this.isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("MatchHistory", "Error: " + t.getMessage());
            }
        });
    }

    private void forceHistoryStatus(MatchPost p) {
        // Mark as MATCHED so the adapter shows the Rate button
        p.setStatus("MATCHED");
    }

    private void showReviewDialog(MatchPost match) {
        boolean isOpponent = MatchPost.TYPE_FIND_OPPONENT.equals(match.getPostType());
        String title = isOpponent ? "Đánh giá đối thủ" : "Đánh giá đồng đội";
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        // Category Spinner (Dropbox)
        android.widget.TextView tvLabelType = new android.widget.TextView(this);
        tvLabelType.setText("Phân loại đánh giá:");
        tvLabelType.setPadding(0, 0, 0, 10);
        layout.addView(tvLabelType);

        android.widget.Spinner spinnerType = new android.widget.Spinner(this);
        String[] types = {"Khen ngợi (Tốt)", "Chơi xấu / Thô lỗ", "Không đến (No Show)", "Khác"};
        String[] values = {"GOOD", "BAD_BEHAVIOR", "NO_SHOW", "OTHER"};
        
        android.widget.ArrayAdapter<String> adapterType = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(adapterType);
        layout.addView(spinnerType);

        // Comment EditText
        android.widget.TextView tvLabelComment = new android.widget.TextView(this);
        tvLabelComment.setText("Nhận xét chi tiết (không bắt buộc):");
        tvLabelComment.setPadding(0, 40, 0, 10);
        layout.addView(tvLabelComment);

        android.widget.EditText etComment = new android.widget.EditText(this);
        etComment.setHint("Nhập ý kiến của bạn...");
        etComment.setMinLines(2);
        etComment.setGravity(android.view.Gravity.TOP);
        layout.addView(etComment);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Gửi đánh giá", (dialog, which) -> {
                    String selectedType = values[spinnerType.getSelectedItemPosition()];
                    String comment = etComment.getText().toString().trim();
                    if (comment.isEmpty()) comment = types[spinnerType.getSelectedItemPosition()];
                    
                    submitReview(match, selectedType, comment);
                })
                .setNegativeButton("Để sau", null)
                .show();
    }

    private void submitReview(MatchPost match, String ratingType, String comment) {
        long currentUserId = sessionManager.getUserId();
        long targetUserId = (match.getUserId() == currentUserId) ? 0 : match.getUserId(); 
        
        ReviewRequest request = new ReviewRequest(targetUserId, match.getId(), ratingType, comment);
        ApiClient.getService(this).submitReview(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MatchHistoryActivity.this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                    loadHistory(); // Refresh to hide Rate button
                } else {
                    Toast.makeText(MatchHistoryActivity.this, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MatchHistoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
