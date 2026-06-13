package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MatchDetailActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────
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

    // ── Data ─────────────────────────────────────────────
    private MatchPost match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_match_detail);
        match = (MatchPost) getIntent().getSerializableExtra(Constants.EXTRA_MATCH);
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

        findViewById(R.id.ivDetailBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnDetailAccept.setOnClickListener(v -> {
            if (match != null && !match.isAccepted()) {
                match.setAccepted(true);
                btnDetailAccept.setText(getString(R.string.match_cta_accepted));
                btnDetailAccept.setEnabled(false);
            }
        });

        btnDetailChat.setOnClickListener(v -> {
            if (match == null) return;
            Conversation conv = new Conversation(
                    match.getIdString(),
                    match.getTeam(),
                    match.getCaptainInitials(),
                    "Bắt đầu cuộc trò chuyện…",
                    "Vừa xong",
                    0,
                    match.getTypeLabel() + " • " + match.getField(),
                    false);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(Constants.EXTRA_CONVERSATION, conv);
            startActivity(intent);
        });
    }

    private void loadData() {
        if (match == null) {
            finish();
            return;
        }

        // Avatar
        int avatarColor = match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                ? R.color.primary : R.color.accent_orange;
        GradientDrawable avatarBg = (GradientDrawable) viewDetailAvatarBg.getBackground().mutate();
        avatarBg.setColor(ContextCompat.getColor(this, avatarColor));
        tvDetailInitials.setText(match.getCaptainInitials());

        // Trust badge
        int trust = match.getTrustScore();
        int trustColor = trust >= 80 ? R.color.trust_high : (trust >= 60 ? R.color.trust_mid : R.color.trust_low);
        GradientDrawable trustBg = (GradientDrawable) tvDetailTrust.getBackground().mutate();
        trustBg.setColor(ContextCompat.getColor(this, trustColor));
        tvDetailTrust.setText(trust + "");

        tvDetailTeam.setText(match.getTeam());
        tvDetailCaptain.setText(match.getCaptain());

        // Badges
        tvDetailLiveBadge.setVisibility(match.isHot() ? View.VISIBLE : View.GONE);
        tvDetailTypeBadge.setText(match.getTypeLabel());
        tvDetailTypeBadge.setBackgroundResource(match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                ? R.drawable.bg_badge_green : R.drawable.bg_badge_orange);
        tvDetailTypeBadge.setTextColor(ContextCompat.getColor(this,
                match.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                        ? R.color.badge_green_text : R.color.badge_orange_text));
        tvDetailLevelBadge.setText(match.getLevel());

        // Match info
        tvDetailDate.setText(match.getDate());
        tvDetailTime.setText(match.getTime());
        tvDetailField.setText(match.getField());
        tvDetailMembers.setText(match.getMembersSlot());
        tvDetailCost.setText(match.getCost());

        // Message
        tvDetailMessage.setText(match.getMessage());

        // Trust stats (mock values derived from trust score)
        tvTrustMatches.setText(String.valueOf(trust * 2));
        tvTrustNoBail.setText(trust >= 80 ? "0" : "1");
        tvTrustRating.setText(String.format(Locale.getDefault(), "%.1f", trust / 20.0));

        // CTA state
        if (match.isAccepted()) {
            btnDetailAccept.setText(getString(R.string.match_cta_accepted));
            btnDetailAccept.setEnabled(false);
        }
    }
}
