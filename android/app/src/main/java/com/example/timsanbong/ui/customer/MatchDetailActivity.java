package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.data.repository.MatchRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchDetailActivity extends AppCompatActivity {

    private View viewDetailAvatarBg, layoutDetailField;
    private TextView tvDetailInitials;
    private TextView tvDetailTrust;
    private TextView tvDetailTeam;
    private TextView tvDetailCaptain;
    private TextView tvDetailLiveBadge;
    private TextView tvDetailTypeBadge;
    private TextView tvDetailLevelBadge;
    private TextView tvDetailDate;
    private TextView tvDetailTime;
    private TextView tvDetailField;
    private TextView tvDetailMembers;
    private TextView tvDetailCost;
    private TextView tvDetailMessage;
    private TextView tvTrustMatches;
    private TextView tvTrustNoBail;
    private TextView tvTrustRating;
    private MaterialButton btnDetailAccept;
    private MaterialButton btnDetailChat;
    private MaterialButton btnDetailDelete;

    private MatchPost match;
    private MatchViewModel matchViewModel;
    private final MatchRepository matchRepository = new MatchRepository();
    private final ChatRepository chatRepository = new ChatRepository();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_match_detail);
        match = (MatchPost) getIntent().getSerializableExtra(Constants.EXTRA_MATCH_POST);
        if (match == null) {
            match = (MatchPost) getIntent().getSerializableExtra(Constants.EXTRA_MATCH);
        }
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        viewDetailAvatarBg = findViewById(R.id.viewDetailAvatarBg);
        tvDetailInitials = findViewById(R.id.tvDetailInitials);
        tvDetailTrust = findViewById(R.id.tvDetailTrust);
        tvDetailTeam = findViewById(R.id.tvDetailTeam);
        tvDetailCaptain = findViewById(R.id.tvDetailCaptain);
        tvDetailLiveBadge = findViewById(R.id.tvDetailLiveBadge);
        tvDetailTypeBadge = findViewById(R.id.tvDetailTypeBadge);
        tvDetailLevelBadge = findViewById(R.id.tvDetailLevelBadge);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailField = findViewById(R.id.tvDetailField);
        tvDetailMembers = findViewById(R.id.tvDetailMembers);
        tvDetailCost = findViewById(R.id.tvDetailCost);
        tvDetailMessage = findViewById(R.id.tvDetailMessage);
        layoutDetailField = (View) findViewById(R.id.tvDetailField).getParent().getParent();
        tvTrustMatches = findViewById(R.id.tvTrustMatches);
        tvTrustNoBail = findViewById(R.id.tvTrustNoBail);
        tvTrustRating = findViewById(R.id.tvTrustRating);
        btnDetailAccept = findViewById(R.id.btnDetailAccept);
        btnDetailChat = findViewById(R.id.btnDetailChat);
        btnDetailDelete = findViewById(R.id.btnDetailDelete);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        sessionManager = new SessionManager(this);

        findViewById(R.id.ivDetailBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnDetailAccept.setOnClickListener(v -> {
            if (match != null && !match.isAccepted()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Xác nhận bắt kèo")
                        .setMessage("Bạn có muốn chuyển đến trang nhắn tin với chủ kèo không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            matchViewModel.createMatchRequest(match.getId(), "");
                            openDirectConversation();
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            matchViewModel.createMatchRequest(match.getId(), "");
                        })
                        .show();
            }
        });

        btnDetailChat.setOnClickListener(v -> openDirectConversation());

        btnDetailDelete.setOnClickListener(v -> {
            if (match != null) {
                matchViewModel.deleteMatchPost(match.getId());
            }
        });

        matchViewModel.matchRequestState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS) {
                match.setAccepted(true);
                matchRepository.saveAcceptedMatchId(this, match.getId());
                btnDetailAccept.setText(getString(R.string.match_cta_accepted));
                btnDetailAccept.setEnabled(false);
                btnDetailAccept.setAlpha(0.5f);
                Toast.makeText(this, "Đã gửi yêu cầu bắt kèo.", Toast.LENGTH_SHORT).show();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        matchViewModel.deleteMatchState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Đã gỡ kèo thành công.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        if (match == null) {
            finish();
            return;
        }

        int avatarColor = match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                ? R.color.primary : R.color.accent_orange;
        GradientDrawable avatarBg = (GradientDrawable) viewDetailAvatarBg.getBackground().mutate();
        avatarBg.setColor(ContextCompat.getColor(this, avatarColor));
        tvDetailInitials.setText(match.getCaptainInitials());

        int trust = match.getTrustScore();
        int trustColor = trust >= 80 ? R.color.trust_high : (trust >= 60 ? R.color.trust_mid : R.color.trust_low);
        GradientDrawable trustBg = (GradientDrawable) tvDetailTrust.getBackground().mutate();
        trustBg.setColor(ContextCompat.getColor(this, trustColor));
        tvDetailTrust.setText(String.valueOf(trust));

        String teamDisplay = match.getTeamName();
        if (teamDisplay == null || teamDisplay.trim().isEmpty()) {
            teamDisplay = match.getTeam();
        }
        tvDetailTeam.setText(teamDisplay);
        
        String captainName = match.getCaptain();
        tvDetailCaptain.setText(getString(R.string.match_captain_role_format, captainName, getString(R.string.default_role)));
        
        tvDetailLiveBadge.setVisibility(match.isHot() ? View.VISIBLE : View.GONE);
        tvDetailTypeBadge.setText(match.getTypeLabel());
        tvDetailTypeBadge.setBackgroundResource(match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                ? R.drawable.bg_badge_green : R.drawable.bg_badge_orange);
        tvDetailTypeBadge.setTextColor(ContextCompat.getColor(this,
                match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                        ? R.color.badge_green_text : R.color.badge_orange_text));
        // Level badge
        String level = match.getLevel();
        if ("INTERMEDIATE".equals(level)) level = "Trung cấp";
        else if ("BEGINNER".equals(level)) level = "Mới chơi";
        else if ("ADVANCED".equals(level)) level = "Nâng cao";
        tvDetailLevelBadge.setText(level);
        tvDetailLevelBadge.setVisibility(level.isEmpty() ? View.GONE : View.VISIBLE);

        tvDetailDate.setText(match.getDate());
        tvDetailTime.setText(match.getTime());
        tvDetailField.setText(match.getField());
        layoutDetailField.setVisibility(Boolean.TRUE.equals(match.getHasField()) ? View.VISIBLE : View.GONE);
        
        if (MatchPost.TYPE_FIND_OPPONENT.equals(match.getType())) {
            ((View) findViewById(R.id.tvDetailMembers).getParent().getParent()).setVisibility(View.GONE);
        } else {
            ((View) findViewById(R.id.tvDetailMembers).getParent().getParent()).setVisibility(View.VISIBLE);
            tvDetailMembers.setText(match.getMembersSlot());
        }

        tvDetailCost.setText(match.getCost());
        tvDetailMessage.setText(match.getMessage());

        tvTrustMatches.setText(String.valueOf(match.getMatchesPlayed()));
        tvTrustNoBail.setText(String.valueOf(match.getNoShows()));
        tvTrustRating.setText(String.format(Locale.getDefault(), "%.1f", match.getAverageRating()));

        if (match.isAccepted() || matchRepository.getAcceptedMatchIds(this).contains(match.getId())) {
            btnDetailAccept.setText(getString(R.string.match_cta_accepted));
            btnDetailAccept.setEnabled(false);
            btnDetailAccept.setAlpha(0.5f);
        }

        // Show delete button if current user is the poster
        if (match.getUserId() == sessionManager.getUserId()) {
            btnDetailDelete.setVisibility(View.VISIBLE);
            btnDetailAccept.setVisibility(View.GONE);
            btnDetailChat.setVisibility(View.GONE);
        } else {
            btnDetailDelete.setVisibility(View.GONE);
            btnDetailAccept.setVisibility(View.VISIBLE);
            btnDetailChat.setVisibility(View.VISIBLE);
        }
    }

    private void openDirectConversation() {
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
                    createNewConversation(autoMessage);
                }
            }

            @Override
            public void onError(String message) {
                createNewConversation(autoMessage);
            }
        });
    }

    private void createNewConversation(String autoMessage) {
        chatRepository.createDirectConversation(this, match.getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                navigateToChat(data, autoMessage);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MatchDetailActivity.this, message, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(MatchDetailActivity.this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CONVERSATION, conversation);
        startActivity(intent);
    }
}
