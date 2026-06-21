package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.data.model.ReviewRequest;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;
import com.example.timsanbong.data.model.Conversation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindOpponentActivity extends AppCompatActivity {

    public static final String TAB_ALL = "ALL";
    public static final String TAB_MY_POSTS = "MY_POSTS";
    public static final String TAB_HISTORY = "HISTORY";

    private TextView tabAll, tabMyPosts, tabHistory;
    private TextView tabSubAll, tabFindOpponent, tabFindMember;
    private View layoutSubFilters;
    private TextView tvEmptyMatches, tvMatchCount;
    private View btnCreatePost, btnQuickFind;

    private MatchAdapter matchAdapter;
    private MatchViewModel matchViewModel;
    private final com.example.timsanbong.data.repository.MatchRepository matchRepository = new com.example.timsanbong.data.repository.MatchRepository();
    private List<MatchPost> allMatches = new ArrayList<>();
    private java.util.Set<Long> localAcceptedIds = new java.util.HashSet<>();
    private List<com.example.timsanbong.data.model.OpponentReviewResponse> myReviews = new ArrayList<>();
    private String currentTab = TAB_ALL;
    private String currentSubFilter = "ALL";
    private final ChatRepository chatRepository = new ChatRepository();
    private NavBarManager navBarManager;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadData();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_find_opponent);

        sessionManager = new SessionManager(this);
        initViews();
        setupListeners();
        setupViewModel();
        selectTab(TAB_ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (matchViewModel != null) {
            localAcceptedIds = matchRepository.getAcceptedMatchIds(this);
            loadData();
        }
    }

    private void initViews() {
        tabAll = findViewById(R.id.tabAll);
        tabMyPosts = findViewById(R.id.tabMyPosts);
        tabHistory = findViewById(R.id.tabHistory);
        
        tabSubAll = findViewById(R.id.tabSubAll);
        tabFindOpponent = findViewById(R.id.tabFindOpponent);
        tabFindMember = findViewById(R.id.tabFindMember);
        layoutSubFilters = findViewById(R.id.layoutSubFilters);
        
        RecyclerView rvMatches = findViewById(R.id.rvMatches);
        tvEmptyMatches = findViewById(R.id.tvEmptyMatches);
        tvMatchCount = findViewById(R.id.tvMatchCount);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        btnQuickFind = findViewById(R.id.btnQuickFind);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        matchAdapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {
                new MaterialAlertDialogBuilder(FindOpponentActivity.this)
                        .setTitle("Xác nhận bắt kèo")
                        .setMessage("Bạn có muốn chuyển đến trang nhắn tin với chủ kèo không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            matchRepository.saveAcceptedMatchId(FindOpponentActivity.this, match.getId());
                            matchViewModel.createMatchRequest(match.getId(), "Tôi muốn bắt kèo này!");
                            openDirectConversation(match);
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            matchRepository.saveAcceptedMatchId(FindOpponentActivity.this, match.getId());
                            matchViewModel.createMatchRequest(match.getId(), "Tôi muốn bắt kèo này!");
                        })
                        .show();
            }

            @Override
            public void onChat(MatchPost match) {
                openDirectConversation(match);
            }

            @Override
            public void onCardClick(MatchPost match) {
                Intent intent = new Intent(FindOpponentActivity.this, MatchDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MATCH_POST, match);
                startForResult.launch(intent);
            }

            @Override
            public void onRate(MatchPost match) {
                showReviewDialog(match);
            }
        });
        matchAdapter.setCurrentUserId(sessionManager.getUserId());
        rvMatches.setAdapter(matchAdapter);

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_MATCHMAKING);
        navBarManager.setup();
    }

    private void setupListeners() {
        tabAll.setOnClickListener(v -> selectTab(TAB_ALL));
        tabMyPosts.setOnClickListener(v -> selectTab(TAB_MY_POSTS));
        tabHistory.setOnClickListener(v -> selectTab(TAB_HISTORY));
        
        tabSubAll.setOnClickListener(v -> selectSubFilter("ALL"));
        tabFindOpponent.setOnClickListener(v -> selectSubFilter(MatchPost.TYPE_FIND_OPPONENT));
        tabFindMember.setOnClickListener(v -> selectSubFilter(MatchPost.TYPE_FIND_MEMBER));

        btnCreatePost.setOnClickListener(v ->
                startForResult.launch(new Intent(this, CreateMatchPostActivity.class)));
        btnQuickFind.setOnClickListener(v -> {
            QuickFindBottomSheetFragment fragment = new QuickFindBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), "QuickFind");
        });
    }

    private void setupViewModel() {
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        matchViewModel.matchPostsState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                localAcceptedIds = matchRepository.getAcceptedMatchIds(this);
                allMatches = resource.data;
                filterAndDisplay();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        matchViewModel.matchRequestState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
                loadData(); // This will refresh localAcceptedIds and list
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        // Always load ALL match posts to ensure we can see accepted ones in history
        matchViewModel.loadMatchPosts(0, 50, null);
        
        ApiClient.getService(this).getMyFairplayReviews().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<com.example.timsanbong.data.model.OpponentReviewResponse>> call, @NonNull Response<List<com.example.timsanbong.data.model.OpponentReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myReviews = response.body();
                    filterAndDisplay();
                }
            }
            @Override public void onFailure(@NonNull Call<List<com.example.timsanbong.data.model.OpponentReviewResponse>> call, @NonNull Throwable t) {}
        });
    }

    private void selectTab(String type) {
        currentTab = type;
        
        tabAll.setBackgroundResource(R.drawable.bg_chip_filter);
        tabAll.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabMyPosts.setBackgroundResource(R.drawable.bg_chip_filter);
        tabMyPosts.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabHistory.setBackgroundResource(R.drawable.bg_chip_filter);
        tabHistory.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        if (TAB_ALL.equals(type)) {
            tabAll.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabAll.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
            layoutSubFilters.setVisibility(View.VISIBLE);
        } else {
            layoutSubFilters.setVisibility(View.GONE);
            
            if (TAB_MY_POSTS.equals(type)) {
                tabMyPosts.setBackgroundResource(R.drawable.bg_chip_filter_selected);
                tabMyPosts.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
            } else if (TAB_HISTORY.equals(type)) {
                tabHistory.setBackgroundResource(R.drawable.bg_chip_filter_selected);
                tabHistory.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
            }
        }
        
        currentSubFilter = "ALL";
        updateSubFilterUI();
        loadData();
    }

    private void selectSubFilter(String filter) {
        currentSubFilter = filter;
        updateSubFilterUI();
        filterAndDisplay();
    }

    private void updateSubFilterUI() {
        tabSubAll.setBackgroundResource(R.drawable.bg_chip_filter);
        tabSubAll.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabFindOpponent.setBackgroundResource(R.drawable.bg_chip_filter);
        tabFindOpponent.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tabFindMember.setBackgroundResource(R.drawable.bg_chip_filter);
        tabFindMember.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        if ("ALL".equals(currentSubFilter)) {
            tabSubAll.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabSubAll.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
        } else if (MatchPost.TYPE_FIND_OPPONENT.equals(currentSubFilter)) {
            tabFindOpponent.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabFindOpponent.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
        } else if (MatchPost.TYPE_FIND_MEMBER.equals(currentSubFilter)) {
            tabFindMember.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabFindMember.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
        }
    }

    private void filterAndDisplay() {
        List<MatchPost> filtered = new ArrayList<>();
        long currentUserId = sessionManager.getUserId();

        // Debug logging to track filtering logic
        android.util.Log.d("FindOpponent", "Filtering. Tab: " + currentTab + ", Sub: " + currentSubFilter);
        android.util.Log.d("FindOpponent", "Local Accepted IDs: " + localAcceptedIds);

        java.util.Map<Long, String> statuses = new java.util.HashMap<>();
        for (com.example.timsanbong.data.model.OpponentReviewResponse r : myReviews) {
            statuses.put(r.getMatchId(), r.getStatus());
        }
        matchAdapter.setReviewStatuses(statuses);
        matchAdapter.setLocalAcceptedIds(localAcceptedIds);

        for (MatchPost match : allMatches) {
            boolean isMyPost = match.getUserId() == currentUserId;
            boolean isAcceptedByMe = (match.isAccepted() || localAcceptedIds.contains(match.getId())) 
                    && !isMyPost;

            if (TAB_ALL.equals(currentTab)) {
                // Board: show open matches. Do NOT hide accepted ones anymore per user request.
                // But we still filter sub-categories.
                if (("OPEN".equals(match.getStatus()) || match.getStatus() == null)) {
                    // Apply sub-filter
                    if ("ALL".equals(currentSubFilter) || currentSubFilter.equals(match.getPostType())) {
                        filtered.add(match);
                    }
                }
            } else if (TAB_MY_POSTS.equals(currentTab)) {
                // My posts: show my posts that are still active
                if (isMyPost && !"CLOSED".equals(match.getStatus())) {
                    filtered.add(match);
                }
            } else if (TAB_HISTORY.equals(currentTab)) {
                // History: show posts I PARTICIPATED in (accepted), OR my own posts that are closed/matched
                boolean isMyClosedPost = isMyPost && ("CLOSED".equals(match.getStatus()) || "MATCHED".equals(match.getStatus()));
                
                if (isAcceptedByMe || isMyClosedPost) {
                    if (isAcceptedByMe) {
                        match.setStatus("MATCHED");
                    }
                    filtered.add(match);
                }
            }
        }
        
        android.util.Log.d("FindOpponent", "Filtered list size: " + filtered.size());
        matchAdapter.updateMatches(filtered);
        tvEmptyMatches.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvMatchCount.setText(getString(R.string.match_count, filtered.size()));
    }

    private void openDirectConversation(MatchPost match) {
        if (match == null || match.getUserId() <= 0) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

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
                Toast.makeText(FindOpponentActivity.this, message, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(FindOpponentActivity.this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CONVERSATION, conversation);
        startActivity(intent);
    }

    private void showReviewDialog(MatchPost match) {
        boolean isOpponent = MatchPost.TYPE_FIND_OPPONENT.equals(match.getPostType());
        String title = isOpponent ? "Đánh giá đối thủ" : "Đánh giá đồng đội";
        
        String[] options = {"Tốt - Chơi đẹp", "Không đến sân (No-show)", "Hành vi xấu / Cay cú"};
        final int[] selectedOption = {0};

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setSingleChoiceItems(options, 0, (dialog, which) -> selectedOption[0] = which)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String ratingType = "GOOD";
                    if (selectedOption[0] == 1) ratingType = "NO_SHOW";
                    else if (selectedOption[0] == 2) ratingType = "BAD_BEHAVIOR";
                    
                    submitReview(match, ratingType);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void submitReview(MatchPost match, String ratingType) {
        long currentUserId = sessionManager.getUserId();
        long targetUserId = (match.getUserId() == currentUserId) ? 0 : match.getUserId(); 
        
        ReviewRequest request = new ReviewRequest(targetUserId, match.getId(), ratingType, "Đánh giá từ lịch sử app");

        ApiClient.getService(this).submitReview(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FindOpponentActivity.this, "Đã gửi đánh giá lên Tòa án Fairplay!", Toast.LENGTH_SHORT).show();
                    loadData(); // Refresh list to update status
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }
}
