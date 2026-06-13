package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.timsanbong.R;

import java.util.List;

public class SuggestedFieldAdapter extends RecyclerView.Adapter<SuggestedFieldAdapter.SuggestedFieldViewHolder> {

    public interface OnSuggestedFieldClickListener {
        void onClick(SuggestedFieldItem item);
    }

    private List<SuggestedFieldItem> items;
    private final OnSuggestedFieldClickListener listener;

    public SuggestedFieldAdapter(List<SuggestedFieldItem> items, OnSuggestedFieldClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<SuggestedFieldItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestedFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_suggested_field, parent, false);
        return new SuggestedFieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedFieldViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SuggestedFieldViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFieldImage;
        private final TextView tvFieldType;
        private final TextView tvHotBadge;
        private final TextView tvFieldName;
        private final TextView tvFieldRating;

        SuggestedFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFieldImage = itemView.findViewById(R.id.ivFieldImage);
            tvFieldType = itemView.findViewById(R.id.tvFieldType);
            tvHotBadge = itemView.findViewById(R.id.tvHotBadge);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldRating = itemView.findViewById(R.id.tvFieldRating);
        }

        void bind(SuggestedFieldItem item) {
            tvFieldName.setText(item.getName());
            tvFieldType.setText(item.getTypeLabel());
            tvHotBadge.setVisibility(item.isHot() ? View.VISIBLE : View.GONE);
            tvFieldRating.setText(itemView.getContext().getString(
                    R.string.rating_distance_format, item.getRating(), item.getDistance()));

            String imageUrl = item.getImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(R.drawable.bg_pitch_cover)
                        .centerCrop()
                        .into(ivFieldImage);
            } else {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_pitch_cover)
                        .error(R.drawable.bg_pitch_cover)
                        .centerCrop()
                        .into(ivFieldImage);
            }

            itemView.setOnClickListener(v -> listener.onClick(item));
        }
    }
}

