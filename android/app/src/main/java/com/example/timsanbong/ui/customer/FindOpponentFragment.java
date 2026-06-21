package com.example.timsanbong.ui.customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.data.repository.MatchRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindOpponentFragment extends Fragment {

    private static final String SCOPE_ALL = "SCOPE_ALL";
    private static final String SCOPE_MY = "SCOPE_MY";
    private static final String SCOPE_HISTORY = "SCOPE_HISTORY";

    private static final String TYPE_ALL = "TYPE_ALL";
    private static final String TYPE_OPPONENT = MatchPost.TYPE_FIND_OPPONENT;
    private static final String TYPE_MEMBER = MatchPost.TYPE_FIND_MEMBER;

    private TextView tabScopeAll, tabScopeMy, tabScopeHistory;
    private TextView tabTypeAll, tabTypeOpponent, tabTypeMember;
    private RecyclerView rvMatches;
    private TextView tvEmptyMatches, tvMatchCount;
    private View btnCreatePost, btnQuickFind;

    private MatchAdapter matchAdapter;
    private MatchViewModel matchViewModel;
    private List<MatchPost> allMatches = new ArrayList<>();
    private String currentScope = SCOPE_ALL;
    private String currentType = TYPE_ALL;
    private final ChatRepository chatRepository = new ChatRepository();
    private final MatchRepository matchRepository = new MatchRepository();
    private Set<Long> localAcceptedIds = new HashSet<>();
    private SessionManager sessionManager;
    private long pendingAcceptMatchId = -1;

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadData();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_customer_find_opponent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        localAcceptedIds = matchRepository.getAcceptedMatchIds(requireContext());

        initViews(view);
        setupListeners();
        setupViewModel();
        updateTabsUI();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLocalAcceptedIds();
        loadData();
    }

    private void initViews(View view) {
        tabScopeAll = view.findViewById(R.id.tabScopeAll);
        tabScopeMy = view.findViewById(R.id.tabScopeMy);
        tabScopeHistory = view.findViewById(R.id.tabScopeHistory);
        
        tabTypeAll = view.findViewById(R.id.tabTypeAll);
        tabTypeOpponent = view.findViewById(R.id.tabTypeOpponent);
        tabTypeMember = view.findViewById(R.id.tabTypeMember);

        rvMatches = view.findViewById(R.id.rvMatches);
        tvEmptyMatches = view.findViewById(R.id.tvEmptyMatches);
        tvMatchCount = view.findViewById(R.id.tvMatchCount);
        btnCreatePost = view.findViewById(R.id.btnCreatePost);
        btnQuickFind = view.findViewById(R.id.btnQuickFind);

        rvMatches.setLayoutManager(new LinearLayoutManager(requireContext()));
        matchAdapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xác nhận bắt kèo")
                        .setMessage("Bạn có muốn chuyển đến trang nhắn tin với chủ kèo không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            pendingAcceptMatchId = match.getId();
                            matchViewModel.createMatchRequest(match.getId(), "Tôi muốn bắt kèo này!");
                            openDirectConversation(match, true);
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            pendingAcceptMatchId = match.getId();
                            matchViewModel.createMatchRequest(match.getId(), "Tôi muốn bắt kèo này!");
                        })
                        .show();
            }

            @Override
            public void onChat(MatchPost match) {
                openDirectConversation(match, false);
            }

            @Override
            public void onCardClick(MatchPost match) {
                Intent intent = new Intent(requireContext(), MatchDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MATCH_POST, match);
                startForResult.launch(intent);
            }
        });
        matchAdapter.setCurrentUserId(sessionManager.getUserId());
        matchAdapter.setLocalAcceptedIds(localAcceptedIds);
        rvMatches.setAdapter(matchAdapter);
    }

    private void setupListeners() {
        tabScopeAll.setOnClickListener(v -> selectScope(SCOPE_ALL));
        tabScopeMy.setOnClickListener(v -> selectScope(SCOPE_MY));
        tabScopeHistory.setOnClickListener(v -> selectScope(SCOPE_HISTORY));

        tabTypeAll.setOnClickListener(v -> selectType(TYPE_ALL));
        tabTypeOpponent.setOnClickListener(v -> selectType(TYPE_OPPONENT));
        tabTypeMember.setOnClickListener(v -> selectType(TYPE_MEMBER));

        btnCreatePost.setOnClickListener(v ->
                startForResult.launch(new Intent(requireContext(), CreateMatchPostActivity.class)));
        btnQuickFind.setOnClickListener(v -> {
            QuickFindBottomSheetFragment fragment = new QuickFindBottomSheetFragment();
            fragment.show(getChildFragmentManager(), "QuickFind");
        });
    }

    private void setupViewModel() {
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        matchViewModel.matchPostsState.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                allMatches = resource.data;
                refreshLocalAcceptedIds();
                filterAndDisplay();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        matchViewModel.matchRequestState.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                if (pendingAcceptMatchId != -1) {
                    matchRepository.saveAcceptedMatchId(requireContext(), pendingAcceptMatchId);
                    pendingAcceptMatchId = -1;
                }
                Toast.makeText(requireContext(), "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
                refreshLocalAcceptedIds();
                loadData();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                pendingAcceptMatchId = -1;
            }
        });
    }

    private void refreshLocalAcceptedIds() {
        localAcceptedIds = matchRepository.getAcceptedMatchIds(requireContext());
        if (matchAdapter != null) {
            matchAdapter.setLocalAcceptedIds(localAcceptedIds);
            matchAdapter.setCurrentUserId(sessionManager.getUserId());
        }
    }

    private void loadData() {
        matchViewModel.loadMatchPosts(0, 50, null);
    }

    private void selectScope(String scope) {
        currentScope = scope;
        updateTabsUI();
        filterAndDisplay();
    }

    private void selectType(String type) {
        currentType = type;
        updateTabsUI();
        filterAndDisplay();
    }

    private void updateTabsUI() {
        // Update Scope Row
        TextView[] scopeTabs = {tabScopeAll, tabScopeMy, tabScopeHistory};
        String[] scopeValues = {SCOPE_ALL, SCOPE_MY, SCOPE_HISTORY};
        for (int i = 0; i < scopeTabs.length; i++) {
            boolean active = currentScope.equals(scopeValues[i]);
            scopeTabs[i].setBackgroundResource(active ? R.drawable.bg_chip_filter_selected : R.drawable.bg_chip_filter);
            scopeTabs[i].setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.text_on_primary : R.color.text_secondary));
            scopeTabs[i].setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        // Update Type Row
        TextView[] typeTabs = {tabTypeAll, tabTypeOpponent, tabTypeMember};
        String[] typeValues = {TYPE_ALL, TYPE_OPPONENT, TYPE_MEMBER};
        for (int i = 0; i < typeTabs.length; i++) {
            boolean active = currentType.equals(typeValues[i]);
            typeTabs[i].setBackgroundResource(active ? R.drawable.bg_chip_filter_selected : R.drawable.bg_chip_filter);
            typeTabs[i].setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.text_on_primary : R.color.text_secondary));
            typeTabs[i].setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        if (matchAdapter != null) {
            matchAdapter.setHistoryMode(SCOPE_HISTORY.equals(currentScope));
        }
    }

    private void filterAndDisplay() {
        List<MatchPost> filtered = new ArrayList<>();
        long currentUserId = sessionManager.getUserId();

        for (MatchPost match : allMatches) {
            // Scope Filter
            boolean passScope = false;
            switch (currentScope) {
                case SCOPE_ALL: passScope = true; break;
                case SCOPE_MY: if (match.getUserId() == currentUserId) passScope = true; break;
                case SCOPE_HISTORY: if (localAcceptedIds.contains(match.getId())) passScope = true; break;
            }
            if (!passScope) continue;

            // Type Filter
            boolean passType = false;
            switch (currentType) {
                case TYPE_ALL: passType = true; break;
                case TYPE_OPPONENT: if (MatchPost.TYPE_FIND_OPPONENT.equals(match.getPostType())) passType = true; break;
                case TYPE_MEMBER: if (MatchPost.TYPE_FIND_MEMBER.equals(match.getPostType())) passType = true; break;
            }
            if (!passType) continue;

            filtered.add(match);
        }
        matchAdapter.updateMatches(filtered);
        tvEmptyMatches.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvMatchCount.setText(getString(R.string.match_count, filtered.size()));
    }

    private void openDirectConversation(MatchPost match, boolean sendAutoMessage) {
        if (match.getUserId() <= 0) {
            Toast.makeText(requireContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.getConversations(requireContext(), new RepositoryCallback<List<Conversation>>() {
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
                    handleConversationNavigation(existing, match, sendAutoMessage);
                } else {
                    createNewConversation(match, sendAutoMessage);
                }
            }

            @Override
            public void onError(String message) {
                createNewConversation(match, sendAutoMessage);
            }
        });
    }

    private void createNewConversation(MatchPost match, boolean sendAutoMessage) {
        chatRepository.createDirectConversation(requireContext(), match.getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                handleConversationNavigation(data, match, sendAutoMessage);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FindOpponentFragment.this.requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleConversationNavigation(Conversation conversation, MatchPost match, boolean sendAutoMessage) {
        if (sendAutoMessage) {
            String autoMessage = String.format("Tôi muốn bắt kèo của bạn: %s - %s - %s",
                    match.getTeam(), match.getDate(), match.getTime());
            
            long convId;
            try {
                convId = Long.parseLong(conversation.getId());
            } catch (Exception e) {
                convId = 0;
            }

            if (convId > 0) {
                MessageRequest request = new MessageRequest(convId, autoMessage);
                chatRepository.sendMessage(requireContext(), request, new RepositoryCallback<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage data) {
                        startChatActivity(conversation);
                    }

                    @Override
                    public void onError(String message) {
                        startChatActivity(conversation);
                    }
                });
                return;
            }
        }
        startChatActivity(conversation);
    }

    private void startChatActivity(Conversation conversation) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CONVERSATION, conversation);
        startActivity(intent);
    }
}
