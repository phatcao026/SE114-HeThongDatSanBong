package com.example.timsanbong.ui.customer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    private final List<MatchPost> matches;
    private final OnMatchActionListener listener;

    public MatchAdapter(List<MatchPost> matches, OnMatchActionListener listener) {
        this.matches = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchPost match = matches.get(position);
        Context ctx = holder.itemView.getContext();

        // Avatar
        int avatarColor = ContextCompat.getColor(ctx,
                MatchPost.TYPE_FIND_OPPONENT.equals(match.getType()) ? R.color.primary : R.color.accent_orange);
        GradientDrawable ovalBg = (GradientDrawable) ContextCompat.getDrawable(ctx, R.drawable.shape_oval).mutate();
        ovalBg.setColor(avatarColor);
        holder.viewAvatarBg.setBackground(ovalBg);
        holder.tvInitials.setText(match.getCaptainInitials());

        // Trust badge
        int trust = match.getTrustScore();
        int trustColor = trust >= 80
                ? ContextCompat.getColor(ctx, R.color.trust_high)
                : trust >= 60
                ? ContextCompat.getColor(ctx, R.color.trust_mid)
                : ContextCompat.getColor(ctx, R.color.trust_low);
        GradientDrawable trustBg = (GradientDrawable) ContextCompat.getDrawable(ctx, R.drawable.bg_trust_badge).mutate();
        trustBg.setColor(trustColor);
        holder.tvTrustBadge.setBackground(trustBg);
        holder.tvTrustBadge.setText(String.valueOf(trust));

        // Team name
        holder.tvTeamName.setText(match.getTeam());

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
        holder.tvLevelBadge.setText(match.getLevel());

        // Posted time
        holder.tvPostedTime.setText(match.getPostedAgo());

        // Message
        holder.tvMatchMessage.setText(match.getMessage());

        // Info
        holder.tvDate.setText(match.getDate());
        holder.tvTime.setText(match.getTime());
        holder.tvField.setText(match.getField());
        holder.tvCost.setText(match.getCost());
        holder.tvMembers.setText(match.getMembersSlot());

        // Accept state
        if (match.isAccepted()) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnChatMatch.setVisibility(View.GONE);
            holder.tvAccepted.setVisibility(View.VISIBLE);
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnChatMatch.setVisibility(View.VISIBLE);
            holder.tvAccepted.setVisibility(View.GONE);
        }

        // Listeners
        holder.itemView.setOnClickListener(v -> listener.onCardClick(match));
        holder.btnAccept.setOnClickListener(v -> listener.onAccept(match, holder.getAdapterPosition()));
        holder.btnChatMatch.setOnClickListener(v -> listener.onChat(match));
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
        final MaterialButton btnAccept;
        final MaterialButton btnChatMatch;
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
            btnAccept = view.findViewById(R.id.btnAccept);
            btnChatMatch = view.findViewById(R.id.btnChatMatch);
            tvAccepted = view.findViewById(R.id.tvAccepted);
        }
    }
}
