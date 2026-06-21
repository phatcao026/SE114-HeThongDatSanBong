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
    private java.util.Map<Long, String> reviewStatuses = new java.util.HashMap<>();

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
        
        // Fetch my submitted reviews to know which matches are already rated
        ApiClient.getService(this).getMySubmittedReviews().enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                reviewStatuses.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (Long matchId : response.body()) {
                        reviewStatuses.put(matchId, "SUBMITTED");
                    }
                }
                adapter.setReviewStatuses(reviewStatuses);
                fetchMatches(acceptedIds);
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                fetchMatches(acceptedIds);
            }
        });
    }

    private void fetchMatches(java.util.Set<Long> acceptedIds) {
        // We need to fetch both "my posts" and "posts I accepted".
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
                            forceHistoryStatus(p);
                            history.add(p);
                        }
                    }
                    adapter.updateMatches(history);
                    tvEmpty.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<MatchPost>> call, Throwable t) {
                if (!MatchHistoryActivity.this.isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void forceHistoryStatus(MatchPost p) {
        // Mark as MATCHED so the adapter shows the Rate button
        p.setStatus("MATCHED");
    }

    private void showReviewDialog(MatchPost match) {
        long myUserId = sessionManager.getUserId();
        boolean isOwner = match.getUserId() == myUserId;
        String title = isOwner ? "Đánh giá đối thủ/cầu thủ" : "Đánh giá chủ kèo";
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        // Rating Type Selection (Spinner / Dropdown)
        android.widget.TextView tvLabelType = new android.widget.TextView(this);
        tvLabelType.setText("Phân loại đánh giá:");
        tvLabelType.setPadding(0, 0, 0, 10);
        tvLabelType.setTextSize(16);
        tvLabelType.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvLabelType);

        android.widget.Spinner spinnerType = new android.widget.Spinner(this);
        String[] displayTypes = {"Tích cực (Khen ngợi)", "Chơi xấu / Thô lỗ", "Không đến (No Show)", "Hủy kèo muộn", "Khác"};
        String[] backendValues = {"GOOD", "BAD_BEHAVIOR", "NO_SHOW", "LATE_CANCEL", "OTHER"};

        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, displayTypes);
        spinnerType.setAdapter(spinnerAdapter);
        layout.addView(spinnerType);

        // Comment EditText
        android.widget.TextView tvLabelComment = new android.widget.TextView(this);
        tvLabelComment.setText("Chi tiết lý do (Admin sẽ xem xét):");
        tvLabelComment.setPadding(0, 40, 0, 10);
        layout.addView(tvLabelComment);

        android.widget.EditText etComment = new android.widget.EditText(this);
        etComment.setHint("Nhập lý do hoặc nhận xét của bạn...");
        etComment.setMinLines(3);
        etComment.setGravity(android.view.Gravity.TOP);
        etComment.setBackgroundResource(android.R.drawable.edit_text);
        layout.addView(etComment);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Gửi đánh giá", (dialog, which) -> {
                    String selectedType = backendValues[spinnerType.getSelectedItemPosition()];
                    String comment = etComment.getText().toString().trim();
                    if (comment.isEmpty()) comment = displayTypes[spinnerType.getSelectedItemPosition()];
                    
                    submitReview(match, selectedType, comment);
                })
                .setNegativeButton("Hủy", null)
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
