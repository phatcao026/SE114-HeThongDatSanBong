package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class MatchmakingActivity extends AppCompatActivity {

    private TextView tabAll, tabOpponent, tabMember, tabSuggested;
    private TextView tvMatchCount;
    private RecyclerView rvMatches;
    private TextView tvEmptyMatches;
    private View fabCreatePost;
    private NavBarManager navBarManager;

    private MatchAdapter matchAdapter;
    private List<MatchPost> allMatches = new ArrayList<>();
    private int currentTab = 0;
    private MatchViewModel matchViewModel;
    private final ChatRepository chatRepository = new ChatRepository();
    private MatchPost pendingAcceptedMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_matchmaking);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        tabAll = findViewById(R.id.tabAll);
        tabOpponent = findViewById(R.id.tabOpponent);
        tabMember = findViewById(R.id.tabMember);
        tabSuggested = findViewById(R.id.tabSuggested);
        tvMatchCount = findViewById(R.id.tvMatchCount);
        rvMatches = findViewById(R.id.rvMatches);
        tvEmptyMatches = findViewById(R.id.tvEmptyMatches);
        fabCreatePost = findViewById(R.id.fabCreatePost);

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_MATCH);
        navBarManager.setup();

        if (fabCreatePost != null) {
            fabCreatePost.setOnClickListener(v ->
                    startActivity(new Intent(this, CreateMatchPostActivity.class)));
        }

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        matchAdapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {
                pendingAcceptedMatch = match;
                matchViewModel.createMatchRequest(match.getId(), "");
            }

            @Override
            public void onChat(MatchPost match) {
                openDirectConversation(match);
            }

            @Override
            public void onCardClick(MatchPost match) {
                Intent intent = new Intent(MatchmakingActivity.this, MatchDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MATCH_POST, match);
                intent.putExtra(Constants.EXTRA_MATCH_ID, match.getIdString());
                startActivity(intent);
            }
        });
        rvMatches.setAdapter(matchAdapter);
    }

    private void setupListeners() {
        View.OnClickListener tabClick = v -> {
            int id = v.getId();
            if (id == R.id.tabAll) selectTab(0);
            else if (id == R.id.tabOpponent) selectTab(1);
            else if (id == R.id.tabMember) selectTab(2);
            else selectTab(3);
        };
        tabAll.setOnClickListener(tabClick);
        tabOpponent.setOnClickListener(tabClick);
        tabMember.setOnClickListener(tabClick);
        tabSuggested.setOnClickListener(tabClick);

        findViewById(R.id.ivNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
    }

    private void loadData() {
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);

        matchViewModel.matchPostsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                allMatches = resource.data;
                selectTab(currentTab);
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                allMatches = new ArrayList<>();
                selectTab(currentTab);
            }
        });

        matchViewModel.matchRequestState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS) {
                if (pendingAcceptedMatch != null) {
                    pendingAcceptedMatch.setAccepted(true);
                    pendingAcceptedMatch = null;
                }
                matchAdapter.updateMatches(getFilteredList(currentTab));
                Toast.makeText(this, "Da gui yeu cau bat keo.", Toast.LENGTH_SHORT).show();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                pendingAcceptedMatch = null;
            }
        });

        matchViewModel.loadMatchPosts(0, 50, null);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        TextView[] tabs = {tabAll, tabOpponent, tabMember, tabSuggested};
        for (int i = 0; i < tabs.length; i++) {
            boolean active = i == tab;
            tabs[i].setBackgroundResource(active ? R.drawable.bg_segment_active : android.R.color.transparent);
            tabs[i].setTextColor(getColor(active ? R.color.text_on_primary : R.color.text_secondary));
        }

        List<MatchPost> filtered = getFilteredList(tab);
        matchAdapter.updateMatches(filtered);
        tvMatchCount.setText(String.format(getString(R.string.match_count), filtered.size()));
        tvEmptyMatches.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvMatches.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private List<MatchPost> getFilteredList(int tab) {
        List<MatchPost> result = new ArrayList<>();
        for (MatchPost match : allMatches) {
            if (tab == 0) result.add(match);
            else if (tab == 1 && MatchPost.TYPE_FIND_OPPONENT.equals(match.getType())) result.add(match);
            else if (tab == 2 && MatchPost.TYPE_FIND_MEMBER.equals(match.getType())) result.add(match);
            else if (tab == 3 && match.getTrustScore() >= 90) result.add(match);
        }
        return result;
    }

    private void openDirectConversation(MatchPost match) {
        if (match.getUserId() <= 0) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.createDirectConversation(this, match.getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                Intent intent = new Intent(MatchmakingActivity.this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CONVERSATION, data);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MatchmakingActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
