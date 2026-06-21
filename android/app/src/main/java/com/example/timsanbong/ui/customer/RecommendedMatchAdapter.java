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
        holder.tvAiExplanation.setText(recommendation.getAiExplanation());
        
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

            holder.tvMatchMessage.setText(post.getMessage());
            holder.tvDate.setText(post.getDate());
            holder.tvTime.setText(post.getTime());
            holder.tvField.setText(post.getField());
            holder.tvCost.setText(post.getCost());
            holder.tvMembers.setText(post.getMembersSlot());
            holder.tvInitials.setText(post.getCaptainInitials());
            
            int trust = post.getTrustScore();
            holder.tvTrustBadge.setText(String.valueOf(trust));
            int trustColor = trust >= 80 ? R.color.trust_high : (trust >= 60 ? R.color.trust_mid : R.color.trust_low);
            ((GradientDrawable) holder.tvTrustBadge.getBackground().mutate())
                    .setColor(ContextCompat.getColor(holder.itemView.getContext(), trustColor));

            int avatarColor = post.getType().equals(MatchPost.TYPE_FIND_OPPONENT) ? R.color.primary : R.color.accent_orange;
            ((GradientDrawable) holder.viewAvatarBg.getBackground().mutate())
                    .setColor(ContextCompat.getColor(holder.itemView.getContext(), avatarColor));

            if (post.isAccepted()) {
                android.util.Log.d("MatchAdapter", "Binding match " + recommendation.getMatchId() + " as ACCEPTED");
                holder.btnAccept.setText("Đã bắt kèo");
                holder.btnAccept.setEnabled(false);
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pitch_800)));
                holder.btnAccept.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.btnAccept.setAlpha(1.0f);
            } else {
                android.util.Log.d("MatchAdapter", "Binding match " + recommendation.getMatchId() + " as AVAILABLE");
                holder.btnAccept.setText("Bắt kèo");
                holder.btnAccept.setEnabled(true);
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_orange)));
                holder.btnAccept.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.btnAccept.setAlpha(1.0f);
            }

            holder.btnChatMatch.setVisibility(View.VISIBLE);
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
            btnAccept = view.findViewById(R.id.btnAccept);
            btnChatMatch = view.findViewById(R.id.btnChatMatch);
        }
    }
}
