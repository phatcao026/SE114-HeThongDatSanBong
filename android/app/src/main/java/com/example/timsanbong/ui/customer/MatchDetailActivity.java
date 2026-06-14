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
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.RepositoryCallback;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MatchDetailActivity extends AppCompatActivity {

    private View viewDetailAvatarBg;
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

    private MatchPost match;
    private MatchViewModel matchViewModel;
    private final ChatRepository chatRepository = new ChatRepository();

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
        tvTrustMatches = findViewById(R.id.tvTrustMatches);
        tvTrustNoBail = findViewById(R.id.tvTrustNoBail);
        tvTrustRating = findViewById(R.id.tvTrustRating);
        btnDetailAccept = findViewById(R.id.btnDetailAccept);
        btnDetailChat = findViewById(R.id.btnDetailChat);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);

        findViewById(R.id.ivDetailBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnDetailAccept.setOnClickListener(v -> {
            if (match != null && !match.isAccepted()) {
                matchViewModel.createMatchRequest(match.getId(), "");
            }
        });

        btnDetailChat.setOnClickListener(v -> openDirectConversation());

        matchViewModel.matchRequestState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS) {
                match.setAccepted(true);
                btnDetailAccept.setText(getString(R.string.match_cta_accepted));
                btnDetailAccept.setEnabled(false);
                Toast.makeText(this, "Da gui yeu cau bat keo.", Toast.LENGTH_SHORT).show();
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

        tvDetailTeam.setText(match.getTeam());
        tvDetailCaptain.setText(match.getCaptain());
        tvDetailLiveBadge.setVisibility(match.isHot() ? View.VISIBLE : View.GONE);
        tvDetailTypeBadge.setText(match.getTypeLabel());
        tvDetailTypeBadge.setBackgroundResource(match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                ? R.drawable.bg_badge_green : R.drawable.bg_badge_orange);
        tvDetailTypeBadge.setTextColor(ContextCompat.getColor(this,
                match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                        ? R.color.badge_green_text : R.color.badge_orange_text));
        tvDetailLevelBadge.setText(match.getLevel());

        tvDetailDate.setText(match.getDate());
        tvDetailTime.setText(match.getTime());
        tvDetailField.setText(match.getField());
        tvDetailMembers.setText(match.getMembersSlot());
        tvDetailCost.setText(match.getCost());
        tvDetailMessage.setText(match.getMessage());

        tvTrustMatches.setText(String.valueOf(trust * 2));
        tvTrustNoBail.setText(trust >= 80 ? "0" : "1");
        tvTrustRating.setText(String.format(Locale.getDefault(), "%.1f", trust / 20.0));

        if (match.isAccepted()) {
            btnDetailAccept.setText(getString(R.string.match_cta_accepted));
            btnDetailAccept.setEnabled(false);
        }
    }

    private void openDirectConversation() {
        if (match == null || match.getUserId() <= 0) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.createDirectConversation(this, match.getUserId(), new RepositoryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                Intent intent = new Intent(MatchDetailActivity.this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CONVERSATION, data);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MatchDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
