package com.example.timsanbong.ui.customer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.MatchPost;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    public interface OnMatchActionListener {
        void onAccept(MatchPost match, int position);
        void onChat(MatchPost match);
        void onCardClick(MatchPost match);
        default void onRate(MatchPost match) {}
    }

    private final List<MatchPost> matches;
    private final OnMatchActionListener listener;
    private java.util.Map<Long, String> reviewStatuses = new java.util.HashMap<>();
    private long currentUserId = -1;
    private java.util.Set<Long> localAcceptedIds = new java.util.HashSet<>();
    private boolean isHistoryMode = false;

    public MatchAdapter(List<MatchPost> matches, OnMatchActionListener listener) {
        this.matches = matches;
        this.listener = listener;
    }

    public void setHistoryMode(boolean historyMode) {
        this.isHistoryMode = historyMode;
        notifyDataSetChanged();
    }

    public void setLocalAcceptedIds(java.util.Set<Long> acceptedIds) {
        this.localAcceptedIds = acceptedIds;
        notifyDataSetChanged();
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
        notifyDataSetChanged();
    }

    public void setReviewStatuses(java.util.Map<Long, String> statuses) {
        this.reviewStatuses = statuses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchPost match = matches.get(position);
        Context ctx = holder.itemView.getContext();

        // Avatar
        int avatarColor = ContextCompat.getColor(ctx,
                MatchPost.TYPE_FIND_OPPONENT.equals(match.getType()) ? R.color.primary : R.color.accent_orange);
        GradientDrawable ovalBg = (GradientDrawable) holder.viewAvatarBg.getBackground().mutate();
        ovalBg.setColor(avatarColor);
        holder.tvInitials.setText(match.getCaptainInitials());

        // Trust badge
        int trust = match.getTrustScore();
        holder.tvTrustBadge.setText(String.valueOf(trust));
        int trustColor = trust >= 80 ? R.color.trust_high : (trust >= 60 ? R.color.trust_mid : R.color.trust_low);
        ((GradientDrawable) holder.tvTrustBadge.getBackground().mutate()).setColor(ContextCompat.getColor(ctx, trustColor));

        // Team name
        String teamDisplay = match.getTeamName();
        if (teamDisplay == null || teamDisplay.trim().isEmpty()) {
            teamDisplay = match.getTeam();
        }
        holder.tvTeamName.setText(teamDisplay);

        // LIVE badge
        holder.tvLiveBadge.setVisibility(match.isHot() ? View.VISIBLE : View.GONE);

        // Type badge
        holder.tvTypeBadge.setText(match.getTypeLabel());
        if (MatchPost.TYPE_FIND_OPPONENT.equals(match.getType())) {
            holder.tvTypeBadge.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text));
            holder.tvTypeBadge.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_badge_green));
        } else {
            holder.tvTypeBadge.setTextColor(ContextCompat.getColor(ctx, R.color.badge_orange_text));
            holder.tvTypeBadge.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_badge_orange));
        }

        // Level badge
        String level = match.getLevel();
        if ("INTERMEDIATE".equals(level)) level = "Trung cấp";
        else if ("BEGINNER".equals(level)) level = "Mới chơi";
        else if ("ADVANCED".equals(level)) level = "Nâng cao";
        holder.tvLevelBadge.setText(level);
        holder.tvLevelBadge.setVisibility((level == null || level.trim().isEmpty()) ? View.GONE : View.VISIBLE);

        // Posted time
        holder.tvPostedTime.setText(match.getTimeAgo());

        // Message
        String message = match.getMessage();
        holder.tvMatchMessage.setText(message);
        holder.tvMatchMessage.setVisibility((message == null || message.trim().isEmpty()) ? View.GONE : View.VISIBLE);

        // Info
        String date = match.getDate();
        holder.tvDate.setText(date);
        holder.itemView.findViewById(R.id.containerDate).setVisibility((date == null || date.trim().isEmpty()) ? View.GONE : View.VISIBLE);

        String time = match.getTime();
        holder.tvTime.setText(time);
        holder.itemView.findViewById(R.id.containerTime).setVisibility((time == null || time.trim().isEmpty()) ? View.GONE : View.VISIBLE);

        holder.itemView.findViewById(R.id.rowDateTime).setVisibility(
                (holder.itemView.findViewById(R.id.containerDate).getVisibility() == View.VISIBLE ||
                 holder.itemView.findViewById(R.id.containerTime).getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);

        String field = match.getField();
        holder.tvField.setText(field);
        holder.itemView.findViewById(R.id.containerField).setVisibility((field == null || field.trim().isEmpty() || "Sân bóng".equals(field)) ? View.GONE : View.VISIBLE);

        String cost = match.getCost();
        holder.tvCost.setText(cost);
        holder.itemView.findViewById(R.id.containerCost).setVisibility((cost == null || cost.trim().isEmpty()) ? View.GONE : View.VISIBLE);
        
        if (MatchPost.TYPE_FIND_OPPONENT.equals(match.getType())) {
            holder.itemView.findViewById(R.id.containerMembers).setVisibility(View.GONE);
        } else {
            String members = match.getMembersSlot();
            holder.tvMembers.setText(members);
            holder.itemView.findViewById(R.id.containerMembers).setVisibility((members == null || members.trim().isEmpty() || members.startsWith("0")) ? View.GONE : View.VISIBLE);
        }

        holder.layoutInfoGrid.setVisibility(
                (holder.itemView.findViewById(R.id.rowDateTime).getVisibility() == View.VISIBLE ||
                 holder.itemView.findViewById(R.id.containerField).getVisibility() == View.VISIBLE ||
                 holder.itemView.findViewById(R.id.containerCost).getVisibility() == View.VISIBLE ||
                 holder.itemView.findViewById(R.id.containerMembers).getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);

        holder.btnAccept.setText(R.string.match_action_accept);
        holder.btnChatMatch.setText(R.string.match_action_chat);
        
        boolean isHistoryView = isHistoryMode ||
                               "COMPLETED".equals(match.getStatus()) ||
                               "EXPIRED".equals(match.getStatus()) ||
                               "MATCHED".equals(match.getStatus()) ||
                               "CLOSED".equals(match.getStatus());

        String reviewStatus = reviewStatuses.get(match.getId());
        boolean isOwner = currentUserId != -1 && match.getUserId() == currentUserId;
        boolean isAcceptedByMe = localAcceptedIds.contains(match.getId());

        if (isHistoryView) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnChatMatch.setVisibility(View.GONE);
            holder.tvAccepted.setVisibility(View.GONE); // Default
            
            if (reviewStatus != null) {
                holder.btnRate.setVisibility(View.GONE);
                holder.tvAccepted.setVisibility(View.VISIBLE);
                if ("PENDING".equals(reviewStatus) || "SUBMITTED".equals(reviewStatus)) {
                    holder.tvAccepted.setText("Đã gửi đánh giá");
                    holder.tvAccepted.setTextColor(ContextCompat.getColor(ctx, R.color.accent_orange));
                } else {
                    holder.tvAccepted.setText("Đã xử lý (Xong)");
                    holder.tvAccepted.setTextColor(ContextCompat.getColor(ctx, R.color.success));
                }
            } else {
                holder.btnRate.setVisibility(View.VISIBLE);
                holder.btnRate.setText("Đánh giá ngay");
                holder.tvAccepted.setVisibility(View.GONE);
            }
        } else if (isOwner) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnChatMatch.setVisibility(View.GONE);
            holder.tvAccepted.setVisibility(View.GONE);
            holder.btnRate.setVisibility(View.GONE);
        } else if (isAcceptedByMe) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setEnabled(false);
            holder.btnAccept.setAlpha(1.0f);
            holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.pitch_800)));
            holder.btnAccept.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            holder.btnAccept.setText("Đã bắt kèo");
            
            holder.btnChatMatch.setVisibility(View.VISIBLE);
            holder.tvAccepted.setVisibility(View.GONE);
            holder.btnRate.setVisibility(View.GONE);
        } else {
            // Available or accepted by someone else
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setEnabled(true);
            holder.btnAccept.setAlpha(1.0f);
            holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.accent_orange)));
            holder.btnAccept.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            holder.btnAccept.setText(R.string.match_action_accept);

            holder.btnChatMatch.setVisibility(View.VISIBLE);
            holder.tvAccepted.setVisibility(View.GONE);
            holder.btnRate.setVisibility(View.GONE);
        }

        // Listeners
        holder.itemView.setOnClickListener(v -> listener.onCardClick(match));
        holder.btnAccept.setOnClickListener(v -> listener.onAccept(match, holder.getAdapterPosition()));
        holder.btnChatMatch.setOnClickListener(v -> listener.onChat(match));
        holder.btnRate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRate(match);
            }
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public void updateMatches(List<MatchPost> newMatches) {
        matches.clear();
        matches.addAll(newMatches);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View viewAvatarBg;
        final TextView tvInitials;
        final TextView tvTrustBadge;
        final TextView tvTeamName;
        final TextView tvLiveBadge;
        final TextView tvTypeBadge;
        final TextView tvLevelBadge;
        final TextView tvPostedTime;
        final TextView tvMatchMessage;
        final TextView tvDate;
        final TextView tvTime;
        final TextView tvField;
        final TextView tvCost;
        final TextView tvMembers;
        final View layoutInfoGrid;
        final MaterialButton btnAccept;
        final MaterialButton btnChatMatch;
        final MaterialButton btnRate;
        final TextView tvAccepted;

        ViewHolder(View view) {
            super(view);
            viewAvatarBg = view.findViewById(R.id.viewAvatarBg);
            tvInitials = view.findViewById(R.id.tvInitials);
            tvTrustBadge = view.findViewById(R.id.tvTrustBadge);
            tvTeamName = view.findViewById(R.id.tvTeamName);
            tvLiveBadge = view.findViewById(R.id.tvLiveBadge);
            tvTypeBadge = view.findViewById(R.id.tvTypeBadge);
            tvLevelBadge = view.findViewById(R.id.tvLevelBadge);
            tvPostedTime = view.findViewById(R.id.tvPostedTime);
            tvMatchMessage = view.findViewById(R.id.tvMatchMessage);
            tvDate = view.findViewById(R.id.tvDate);
            tvTime = view.findViewById(R.id.tvTime);
            tvField = view.findViewById(R.id.tvField);
            tvCost = view.findViewById(R.id.tvCost);
            tvMembers = view.findViewById(R.id.tvMembers);
            layoutInfoGrid = view.findViewById(R.id.layoutInfoGrid);
            btnAccept = view.findViewById(R.id.btnAccept);
            btnChatMatch = view.findViewById(R.id.btnChatMatch);
            btnRate = view.findViewById(R.id.btnRate);
            tvAccepted = view.findViewById(R.id.tvAccepted);
        }
    }
}
