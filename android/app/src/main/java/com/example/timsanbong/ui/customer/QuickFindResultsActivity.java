package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.RecommendedMatch;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuickFindResultsActivity extends AppCompatActivity {

    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private RecommendedMatchAdapter adapter;
    private MatchViewModel matchViewModel;
    private ChatRepository chatRepository = new ChatRepository();

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadRecommendations();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_find_results);

        rvResults = findViewById(R.id.rvResults);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecommendedMatchAdapter(new ArrayList<>(), new RecommendedMatchAdapter.OnRecommendedActionListener() {
            @Override
            public void onAccept(RecommendedMatch recommendation) {
                acceptMatch(recommendation);
            }

            @Override
            public void onCardClick(RecommendedMatch recommendation) {
                if (recommendation.getMatchPost() != null) {
                    Intent intent = new Intent(QuickFindResultsActivity.this, MatchDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_MATCH_POST, recommendation.getMatchPost());
                    startForResult.launch(intent);
                }
            }
        });
        rvResults.setAdapter(adapter);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        
        // We need to add this LiveData to MatchViewModel if it doesn't exist
        // For now, let's assume it exists or we will add it.
        // Actually, let's check MatchViewModel again.
        
        loadRecommendations();
    }

    private void loadRecommendations() {
        String playstyle = getIntent().getStringExtra("playstyle");
        String teamName = getIntent().getStringExtra("teamName");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        
        // Clean up empty strings to null so they aren't used in search
        if (playstyle != null && playstyle.trim().isEmpty()) playstyle = null;
        if (teamName != null && teamName.trim().isEmpty()) teamName = null;
        if (date != null && date.trim().isEmpty()) date = null;
        if (time != null && time.trim().isEmpty()) {
            time = null;
        } else if (time != null) {
            time = time + ":00"; // Ensure standard time format
        }

        progressBar.setVisibility(View.VISIBLE);
        
        new com.example.timsanbong.data.repository.MatchRepository().getSmartRecommendations(
                this, playstyle, teamName, date, time, null, null, null, null, null,
                new RepositoryCallback<List<RecommendedMatch>>() {
                    @Override
                    public void onSuccess(List<RecommendedMatch> data) {
                        progressBar.setVisibility(View.GONE);
                        adapter.updateList(data);
                        if (tvTitle != null) {
                            if (data.isEmpty()) {
                                tvTitle.setText("Không tìm thấy kèo nào phù hợp");
                            } else {
                                tvTitle.setText(String.format(Locale.getDefault(), "Tìm thấy %d kèo", data.size()));
                            }
                        }
                        if (data.isEmpty()) {
                            Toast.makeText(QuickFindResultsActivity.this, "Không tìm thấy gợi ý phù hợp.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuickFindResultsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void acceptMatch(RecommendedMatch recommendation) {
        if (recommendation.getMatchPost() == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        new com.example.timsanbong.data.repository.MatchRepository().createMatchRequest(
                this, recommendation.getMatchId(), "Tôi muốn bắt kèo này!", 
                new RepositoryCallback<com.example.timsanbong.data.model.MatchRequestResponse>() {
            @Override
            public void onSuccess(com.example.timsanbong.data.model.MatchRequestResponse data) {
                openDirectConversation(recommendation);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuickFindResultsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDirectConversation(RecommendedMatch recommendation) {
        if (recommendation.getMatchPost() == null) return;
        
        chatRepository.createDirectConversation(this, recommendation.getMatchPost().getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                Intent intent = new Intent(QuickFindResultsActivity.this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CONVERSATION, data);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(QuickFindResultsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
