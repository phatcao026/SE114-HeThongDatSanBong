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
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MessageRequest;
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
            public void onChat(RecommendedMatch recommendation) {
                openDirectConversation(recommendation);
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
        String timeStart = getIntent().getStringExtra("time");
        String timeEnd = getIntent().getStringExtra("timeEnd");
        String postType = getIntent().getStringExtra("postType");
        String skillLevel = getIntent().getStringExtra("skillLevel");
        String ageRange = getIntent().getStringExtra("ageRange");
        Boolean hasField = null;
        if (getIntent().hasExtra("hasField")) {
            hasField = getIntent().getBooleanExtra("hasField", false);
        }
        
        // Clean up empty strings to null so they aren't used in search
        if (playstyle != null && playstyle.trim().isEmpty()) playstyle = null;
        if (teamName != null && teamName.trim().isEmpty()) teamName = null;
        if (date != null && date.trim().isEmpty()) date = null;
        if (postType != null && postType.trim().isEmpty()) postType = null;
        if (skillLevel != null && skillLevel.trim().isEmpty()) skillLevel = null;
        if (ageRange != null && ageRange.trim().isEmpty()) ageRange = null;

        progressBar.setVisibility(View.VISIBLE);
        
        java.util.Set<Long> acceptedIds = new com.example.timsanbong.data.repository.MatchRepository().getAcceptedMatchIds(this);

        new com.example.timsanbong.data.repository.MatchRepository().getSmartRecommendations(
                this, playstyle, teamName, date, timeStart, timeEnd, skillLevel, hasField, postType, ageRange,
                new RepositoryCallback<List<RecommendedMatch>>() {
                    @Override
                    public void onSuccess(List<RecommendedMatch> data) {
                        progressBar.setVisibility(View.GONE);
                        android.util.Log.d("QuickFind", "Loaded " + data.size() + " matches. Local accepted IDs: " + acceptedIds);
                        for (RecommendedMatch rm : data) {
                            if (rm.getMatchId() != null && acceptedIds.contains(rm.getMatchId())) {
                                android.util.Log.d("QuickFind", "Marking match " + rm.getMatchId() + " as accepted");
                                rm.setAccepted(true);
                            }
                        }
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
        if (recommendation.getMatchPost() == null || recommendation.isAccepted()) return;

        progressBar.setVisibility(View.VISIBLE);
        new com.example.timsanbong.data.repository.MatchRepository().createMatchRequest(
                this, recommendation.getMatchId(), "Tôi muốn bắt kèo này!",
                new RepositoryCallback<com.example.timsanbong.data.model.MatchRequestResponse>() {
                    @Override
                    public void onSuccess(com.example.timsanbong.data.model.MatchRequestResponse data) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuickFindResultsActivity.this, "Đã gửi yêu cầu bắt kèo.", Toast.LENGTH_SHORT).show();
                        
                        // Save local state
                        android.util.Log.d("QuickFind", "Saving accepted match ID: " + recommendation.getMatchId());
                        new com.example.timsanbong.data.repository.MatchRepository().saveAcceptedMatchId(QuickFindResultsActivity.this, recommendation.getMatchId());
                        recommendation.setAccepted(true);
                        adapter.notifyDataSetChanged();

                        // Show dialog ONLY after successful acceptance
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(QuickFindResultsActivity.this)
                                .setTitle("Xác nhận bắt kèo")
                                .setMessage("Bạn có muốn chuyển đến trang nhắn tin với chủ kèo không?")
                                .setPositiveButton("Có", (dialog, which) -> {
                                    openDirectConversation(recommendation);
                                })
                                .setNegativeButton("Không", null)
                                .show();
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuickFindResultsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void openDirectConversation(RecommendedMatch recommendation) {
        if (recommendation.getMatchPost() == null || recommendation.getMatchPost().getUserId() <= 0) {
            return;
        }

        MatchPost match = recommendation.getMatchPost();
        String autoMessage = String.format("Tôi muốn bắt kèo của bạn: %s - %s - %s",
                match.getTeam(), match.getDate(), match.getTime());

        chatRepository.getConversations(this, new RepositoryCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> data) {
                Conversation existing = null;
                for (Conversation c : data) {
                    if (c.getOtherUser() != null && c.getOtherUser().getId() == match.getUserId()) {
                        existing = c;
                        break;
                    }
                }
                if (existing != null) {
                    navigateToChat(existing, autoMessage);
                } else {
                    createNewConversation(match.getUserId(), autoMessage);
                }
            }

            @Override
            public void onError(String message) {
                createNewConversation(match.getUserId(), autoMessage);
            }
        });
    }

    private void createNewConversation(long userId, String autoMessage) {
        chatRepository.createDirectConversation(this, userId, new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                navigateToChat(data, autoMessage);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(QuickFindResultsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToChat(Conversation conversation, String autoMessage) {
        long conversationId;
        try {
            conversationId = Long.parseLong(conversation.getId());
        } catch (NumberFormatException e) {
            conversationId = 0;
        }

        if (conversationId > 0) {
            MessageRequest request = new MessageRequest(conversationId, autoMessage);
            chatRepository.sendMessage(this, request, new RepositoryCallback<ChatMessage>() {
                @Override
                public void onSuccess(ChatMessage data) {
                    startChatActivity(conversation);
                }

                @Override
                public void onError(String message) {
                    startChatActivity(conversation);
                }
            });
        } else {
            startChatActivity(conversation);
        }
    }

    private void startChatActivity(Conversation conversation) {
        Intent intent = new Intent(QuickFindResultsActivity.this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CONVERSATION, conversation);
        startActivity(intent);
    }
}
