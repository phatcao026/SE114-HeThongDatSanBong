package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.data.model.Conversation;

import java.util.ArrayList;
import java.util.List;

public class FindOpponentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tabAll, tabFindOpponent, tabFindMember;
    private RecyclerView rvMatches;
    private TextView tvEmptyMatches, tvMatchCount;
    private View btnCreatePost, btnQuickFind;

    private MatchAdapter matchAdapter;
    private MatchViewModel matchViewModel;
    private List<MatchPost> allMatches = new ArrayList<>();
    private String currentPostType = "ALL";
    private ChatRepository chatRepository = new ChatRepository();
    private NavBarManager navBarManager;

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

        initViews();
        setupListeners();
        setupViewModel();
        selectTab("ALL");
        loadData();
    }

    private void initViews() {
        tabAll = findViewById(R.id.tabAll);
        tabFindOpponent = findViewById(R.id.tabFindOpponent);
        tabFindMember = findViewById(R.id.tabFindMember);
        rvMatches = findViewById(R.id.rvMatches);
        tvEmptyMatches = findViewById(R.id.tvEmptyMatches);
        tvMatchCount = findViewById(R.id.tvMatchCount);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        btnQuickFind = findViewById(R.id.btnQuickFind);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        matchAdapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {
                matchViewModel.createMatchRequest(match.getId(), "Tôi muốn bắt kèo này!");
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
        });
        rvMatches.setAdapter(matchAdapter);

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_MATCHMAKING);
        navBarManager.setup();
    }

    private void setupListeners() {
        tabAll.setOnClickListener(v -> selectTab("ALL"));
        tabFindOpponent.setOnClickListener(v -> selectTab(MatchPost.TYPE_FIND_OPPONENT));
        tabFindMember.setOnClickListener(v -> selectTab(MatchPost.TYPE_FIND_MEMBER));
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
                allMatches = resource.data;
                filterAndDisplay();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        matchViewModel.matchRequestState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        matchViewModel.loadMatchPosts(0, 50, null);
    }

    private void selectTab(String type) {
        currentPostType = type;
        
        tabAll.setBackgroundResource(R.drawable.bg_chip_filter);
        tabAll.setTextColor(getResources().getColor(R.color.text_secondary));
        tabFindOpponent.setBackgroundResource(R.drawable.bg_chip_filter);
        tabFindOpponent.setTextColor(getResources().getColor(R.color.text_secondary));
        tabFindMember.setBackgroundResource(R.drawable.bg_chip_filter);
        tabFindMember.setTextColor(getResources().getColor(R.color.text_secondary));

        if ("ALL".equals(type)) {
            tabAll.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabAll.setTextColor(getResources().getColor(R.color.text_on_primary));
        } else if (MatchPost.TYPE_FIND_OPPONENT.equals(type)) {
            tabFindOpponent.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabFindOpponent.setTextColor(getResources().getColor(R.color.text_on_primary));
        } else if (MatchPost.TYPE_FIND_MEMBER.equals(type)) {
            tabFindMember.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabFindMember.setTextColor(getResources().getColor(R.color.text_on_primary));
        }
        
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        List<MatchPost> filtered = new ArrayList<>();
        for (MatchPost match : allMatches) {
            if ("ALL".equals(currentPostType)) {
                filtered.add(match);
            } else if (currentPostType.equals(match.getPostType())) {
                filtered.add(match);
            }
        }
        matchAdapter.updateMatches(filtered);
        tvEmptyMatches.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvMatchCount.setText(getString(R.string.match_count, filtered.size()));
    }

    private void openDirectConversation(MatchPost match) {
        chatRepository.createDirectConversation(this, match.getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                Intent intent = new Intent(FindOpponentActivity.this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CONVERSATION, data);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FindOpponentActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
