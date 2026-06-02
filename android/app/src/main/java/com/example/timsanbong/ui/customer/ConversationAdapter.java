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
import com.example.timsanbong.data.model.Conversation;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onClick(Conversation conversation);
    }

    private final List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conv = conversations.get(position);
        Context ctx = holder.itemView.getContext();

        // Avatar
        int avatarColor = ContextCompat.getColor(ctx,
                position == 0 ? R.color.primary
                : position == 1 ? R.color.text_secondary
                : R.color.accent_orange);
        GradientDrawable oval = (GradientDrawable) ContextCompat.getDrawable(ctx, R.drawable.shape_oval).mutate();
        oval.setColor(avatarColor);
        holder.viewConvAvatarBg.setBackground(oval);
        holder.tvConvInitials.setText(conv.getInitials());

        // Online dot
        holder.viewOnlineDot.setVisibility(conv.isOnline() ? View.VISIBLE : View.GONE);

        // Name + time
        holder.tvConvName.setText(conv.getName());
        holder.tvConvTime.setText(conv.getTime());

        // Unread state
        boolean hasUnread = conv.getUnreadCount() > 0;
        holder.tvLastMessage.setText(conv.getLastMessage());
        holder.tvLastMessage.setTextColor(ContextCompat.getColor(ctx,
                hasUnread ? R.color.text_primary : R.color.text_secondary));

        if (hasUnread) {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.tvUnreadBadge.setText(String.valueOf(conv.getUnreadCount()));
            holder.tvConvTime.setTextColor(ContextCompat.getColor(ctx, R.color.primary_dark));
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
            holder.tvConvTime.setTextColor(ContextCompat.getColor(ctx, R.color.text_hint));
        }

        // Match context tag
        if (conv.getMatchContext() != null) {
            holder.tvMatchContext.setVisibility(View.VISIBLE);
            holder.tvMatchContext.setText(conv.getMatchContext());
        } else {
            holder.tvMatchContext.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(conv));
    }

    public void updateConversations(List<Conversation> newList) {
        conversations.clear();
        conversations.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View viewConvAvatarBg;
        final TextView tvConvInitials;
        final View viewOnlineDot;
        final TextView tvConvName;
        final TextView tvConvTime;
        final TextView tvLastMessage;
        final TextView tvUnreadBadge;
        final TextView tvMatchContext;

        ViewHolder(View view) {
            super(view);
            viewConvAvatarBg = view.findViewById(R.id.viewConvAvatarBg);
            tvConvInitials = view.findViewById(R.id.tvConvInitials);
            viewOnlineDot = view.findViewById(R.id.viewOnlineDot);
            tvConvName = view.findViewById(R.id.tvConvName);
            tvConvTime = view.findViewById(R.id.tvConvTime);
            tvLastMessage = view.findViewById(R.id.tvLastMessage);
            tvUnreadBadge = view.findViewById(R.id.tvUnreadBadge);
            tvMatchContext = view.findViewById(R.id.tvMatchContext);
        }
    }
}
