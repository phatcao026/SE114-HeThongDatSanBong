package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    private final List<ChatMessage> messages;

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        holder.layoutSystem.setVisibility(View.GONE);
        holder.layoutThem.setVisibility(View.GONE);
        holder.layoutMe.setVisibility(View.GONE);

        switch (msg.getSide()) {
            case ChatMessage.SIDE_SYSTEM:
                holder.layoutSystem.setVisibility(View.VISIBLE);
                holder.tvSystem.setText(msg.getText());
                break;
            case ChatMessage.SIDE_THEM:
                holder.layoutThem.setVisibility(View.VISIBLE);
                holder.tvThemText.setText(msg.getText());
                holder.tvThemTime.setText(msg.getTime());
                break;
            case ChatMessage.SIDE_ME:
                holder.layoutMe.setVisibility(View.VISIBLE);
                holder.tvMeText.setText(msg.getText());
                holder.tvMeTime.setText(msg.getTime());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout layoutSystem;
        final LinearLayout layoutThem;
        final LinearLayout layoutMe;
        final TextView tvSystem;
        final TextView tvThemText;
        final TextView tvThemTime;
        final TextView tvMeText;
        final TextView tvMeTime;

        ViewHolder(View view) {
            super(view);
            layoutSystem = view.findViewById(R.id.layoutSystem);
            layoutThem = view.findViewById(R.id.layoutThem);
            layoutMe = view.findViewById(R.id.layoutMe);
            tvSystem = view.findViewById(R.id.tvSystem);
            tvThemText = view.findViewById(R.id.tvThemText);
            tvThemTime = view.findViewById(R.id.tvThemTime);
            tvMeText = view.findViewById(R.id.tvMeText);
            tvMeTime = view.findViewById(R.id.tvMeTime);
        }
    }
}
