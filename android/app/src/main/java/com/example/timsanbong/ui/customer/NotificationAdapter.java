package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.AppNotification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
    }

    private final List<AppNotification> notifications;
    private final OnNotificationClickListener listener;

    public void updateNotifications(List<AppNotification> newList) {
        notifications.clear();
        notifications.addAll(newList);
        notifyDataSetChanged();
    }

    public NotificationAdapter(List<AppNotification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification notif = notifications.get(position);
        holder.ivIcon.setImageResource(notif.getIconRes());
        ImageViewCompat.setImageTintList(holder.ivIcon,
                android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.getContext(), notif.getIconTintRes())));
        holder.tvTitle.setText(notif.getTitle());
        holder.tvBody.setText(notif.getBody());
        holder.tvTime.setText(notif.getTime());
        holder.itemView.setAlpha(notif.isRead() ? 0.65f : 1f);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notif);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvBody;
        final TextView tvTime;

        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.ivNotificationIcon);
            tvTitle = view.findViewById(R.id.tvNotifTitle);
            tvBody = view.findViewById(R.id.tvNotifBody);
            tvTime = view.findViewById(R.id.tvNotifTime);
        }
    }
}
