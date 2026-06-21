package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.RecommendedMatch;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RecommendedMatchAdapter extends RecyclerView.Adapter<RecommendedMatchAdapter.ViewHolder> {

    private List<RecommendedMatch> recommendations;
    private OnRecommendedActionListener listener;

    public interface OnRecommendedActionListener {
        void onAccept(RecommendedMatch recommendation);
        void onChat(RecommendedMatch recommendation);
        void onCardClick(RecommendedMatch recommendation);
    }

    public RecommendedMatchAdapter(List<RecommendedMatch> recommendations, OnRecommendedActionListener listener) {
        this.recommendations = recommendations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedMatch recommendation = recommendations.get(position);
        
        String explanation = recommendation.getAiExplanation();
        if (explanation == null || explanation.isEmpty()) {
            explanation = holder.itemView.getContext().getString(R.string.match_suggestion_hint);
        }
        holder.tvAiExplanation.setText(explanation);
        
        MatchPost post = recommendation.getMatchPost();
        if (post != null) {
            // Apply accepted state to post if recommendation is marked as accepted
            if (recommendation.isAccepted()) {
                post.setAccepted(true);
            }

            String teamDisplay = post.getTeamName();
            if (teamDisplay == null || teamDisplay.trim().isEmpty()) {
                teamDisplay = post.getTeam();
            }
            holder.tvTeamName.setText(teamDisplay);

            String message = post.getMessage();
            holder.tvMatchMessage.setText(message);
            holder.tvMatchMessage.setVisibility((message == null || message.trim().isEmpty()) ? View.GONE : View.VISIBLE);

            String date = post.getDate();
            holder.tvDate.setText(date);
            holder.itemView.findViewById(R.id.containerDate).setVisibility((date == null || date.trim().isEmpty()) ? View.GONE : View.VISIBLE);

            String time = post.getTime();
            holder.tvTime.setText(time);
            holder.itemView.findViewById(R.id.containerTime).setVisibility((time == null || time.trim().isEmpty()) ? View.GONE : View.VISIBLE);

            holder.itemView.findViewById(R.id.rowDateTime).setVisibility(
                    (holder.itemView.findViewById(R.id.containerDate).getVisibility() == View.VISIBLE ||
                     holder.itemView.findViewById(R.id.containerTime).getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);

            String field = post.getField();
            holder.tvField.setText(field);
            holder.itemView.findViewById(R.id.containerField).setVisibility((field == null || field.trim().isEmpty() || "Sân bóng".equals(field)) ? View.GONE : View.VISIBLE);

            String cost = post.getCost();
            holder.tvCost.setText(cost);
            holder.itemView.findViewById(R.id.containerCost).setVisibility((cost == null || cost.trim().isEmpty()) ? View.GONE : View.VISIBLE);
            
            if (MatchPost.TYPE_FIND_OPPONENT.equals(post.getType())) {
                holder.itemView.findViewById(R.id.containerMembers).setVisibility(View.GONE);
            } else {
                String members = post.getMembersSlot();
                holder.tvMembers.setText(members);
                holder.itemView.findViewById(R.id.containerMembers).setVisibility((members == null || members.trim().isEmpty() || members.startsWith("0")) ? View.GONE : View.VISIBLE);
            }

            holder.layoutInfoGrid.setVisibility(
                    (holder.itemView.findViewById(R.id.rowDateTime).getVisibility() == View.VISIBLE ||
                     holder.itemView.findViewById(R.id.containerField).getVisibility() == View.VISIBLE ||
                     holder.itemView.findViewById(R.id.containerCost).getVisibility() == View.VISIBLE ||
                     holder.itemView.findViewById(R.id.containerMembers).getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);

            holder.tvInitials.setText(post.getCaptainInitials());
            
            int trust = post.getTrustScore();
            holder.tvTrustBadge.setText(String.valueOf(trust));
            int trustColor = trust >= 80 ? R.color.trust_high : (trust >= 60 ? R.color.trust_mid : R.color.trust_low);
            ((GradientDrawable) holder.tvTrustBadge.getBackground().mutate())
                    .setColor(ContextCompat.getColor(holder.itemView.getContext(), trustColor));

            int avatarColor = post.getType().equals(MatchPost.TYPE_FIND_OPPONENT) ? R.color.primary : R.color.accent_orange;
            ((GradientDrawable) holder.viewAvatarBg.getBackground().mutate())
                    .setColor(ContextCompat.getColor(holder.itemView.getContext(), avatarColor));

            holder.tvTypeBadge.setText(post.getTypeLabel());
            holder.tvTypeBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.tvTypeBadge.setBackgroundResource(post.getType().equals(MatchPost.TYPE_FIND_OPPONENT)
                    ? R.drawable.bg_badge_green : R.drawable.bg_badge_orange);

            // Show calculated match score
            holder.tvMatchScore.setVisibility(View.VISIBLE);
            holder.tvMatchScore.setText(holder.itemView.getContext().getString(R.string.match_score_format, recommendation.getMatchScore()));

            // User Role & Acceptance Logic (Sync with MatchAdapter)
            long currentUserId = new com.example.timsanbong.utils.SessionManager(holder.itemView.getContext()).getUserId();
            boolean isOwner = currentUserId != -1 && post.getUserId() == currentUserId;
            boolean isAcceptedByMe = recommendation.isAccepted() || post.isAccepted();

            if (isOwner) {
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnChatMatch.setVisibility(View.GONE);
            } else if (isAcceptedByMe) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setEnabled(false);
                holder.btnAccept.setAlpha(1.0f);
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pitch_800)));
                holder.btnAccept.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.btnAccept.setText("Đã bắt kèo");
                holder.btnChatMatch.setVisibility(View.VISIBLE);
            } else {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setEnabled(true);
                holder.btnAccept.setAlpha(1.0f);
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_orange)));
                holder.btnAccept.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.btnAccept.setText("Bắt kèo");
                holder.btnChatMatch.setVisibility(View.VISIBLE);
            }

            holder.btnChatMatch.setOnClickListener(v -> listener.onChat(recommendation));
            holder.btnAccept.setOnClickListener(v -> listener.onAccept(recommendation));
            holder.itemView.setOnClickListener(v -> listener.onCardClick(recommendation));
        }
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    public void updateList(List<RecommendedMatch> newList) {
        this.recommendations = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiExplanation;
        View viewAvatarBg;
        TextView tvInitials, tvTrustBadge, tvTeamName, tvMatchMessage;
        TextView tvDate, tvTime, tvField, tvCost, tvMembers;
        MaterialButton btnAccept, btnChatMatch;
        TextView tvTypeBadge, tvMatchScore;
        View layoutInfoGrid;

        ViewHolder(View view) {
            super(view);
            tvAiExplanation = view.findViewById(R.id.tvAiExplanation);
            viewAvatarBg = view.findViewById(R.id.viewAvatarBg);
            tvInitials = view.findViewById(R.id.tvInitials);
            tvTrustBadge = view.findViewById(R.id.tvTrustBadge);
            tvTeamName = view.findViewById(R.id.tvTeamName);
            tvMatchMessage = view.findViewById(R.id.tvMatchMessage);
            tvDate = view.findViewById(R.id.tvDate);
            tvTime = view.findViewById(R.id.tvTime);
            tvField = view.findViewById(R.id.tvField);
            tvCost = view.findViewById(R.id.tvCost);
            tvMembers = view.findViewById(R.id.tvMembers);
            tvTypeBadge = view.findViewById(R.id.tvTypeBadge);
            tvMatchScore = view.findViewById(R.id.tvMatchScore);
            layoutInfoGrid = view.findViewById(R.id.layoutInfoGrid);
            btnAccept = view.findViewById(R.id.btnAccept);
            btnChatMatch = view.findViewById(R.id.btnChatMatch);
        }
    }
}
