package com.example.timsanbong.ui.customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class FindOpponentFragment extends Fragment {

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

        initViews(view);
        setupListeners();
        setupViewModel();
        selectTab("ALL");
        loadData();
    }

    private void initViews(View view) {
        tabAll = view.findViewById(R.id.tabAll);
        tabFindOpponent = view.findViewById(R.id.tabFindOpponent);
        tabFindMember = view.findViewById(R.id.tabFindMember);
        rvMatches = view.findViewById(R.id.rvMatches);
        tvEmptyMatches = view.findViewById(R.id.tvEmptyMatches);
        tvMatchCount = view.findViewById(R.id.tvMatchCount);
        btnCreatePost = view.findViewById(R.id.btnCreatePost);
        btnQuickFind = view.findViewById(R.id.btnQuickFind);

        rvMatches.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                Intent intent = new Intent(requireContext(), MatchDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MATCH_POST, match);
                startForResult.launch(intent);
            }
        });
        rvMatches.setAdapter(matchAdapter);
    }

    private void setupListeners() {
        tabAll.setOnClickListener(v -> selectTab("ALL"));
        tabFindOpponent.setOnClickListener(v -> selectTab(MatchPost.TYPE_FIND_OPPONENT));
        tabFindMember.setOnClickListener(v -> selectTab(MatchPost.TYPE_FIND_MEMBER));
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
                filterAndDisplay();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        matchViewModel.matchRequestState.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        matchViewModel.loadMatchPosts(0, 50, null);
    }

    private void selectTab(String type) {
        currentPostType = type;
        
        tabAll.setBackgroundResource(R.drawable.bg_chip_filter);
        tabAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        tabFindOpponent.setBackgroundResource(R.drawable.bg_chip_filter);
        tabFindOpponent.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        tabFindMember.setBackgroundResource(R.drawable.bg_chip_filter);
        tabFindMember.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        if ("ALL".equals(type)) {
            tabAll.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        } else if (MatchPost.TYPE_FIND_OPPONENT.equals(type)) {
            tabFindOpponent.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabFindOpponent.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        } else if (MatchPost.TYPE_FIND_MEMBER.equals(type)) {
            tabFindMember.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            tabFindMember.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
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
        chatRepository.createDirectConversation(requireContext(), match.getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CONVERSATION, data);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
