package com.example.timsanbong.ui.owner;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerFieldAdapter extends RecyclerView.Adapter<OwnerFieldAdapter.OwnerFieldViewHolder> {
    public interface Listener {
        void onEditField(Field field);
        void onDeleteField(Field field);
        void onManageTimeSlots(Field field);
    }

    private final List<Field> fields = new ArrayList<>();
    private final Listener listener;

    public OwnerFieldAdapter(Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Field> newFields) {
        fields.clear();
        if (newFields != null) {
            fields.addAll(newFields);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OwnerFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_field, parent, false);
        return new OwnerFieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerFieldViewHolder holder, int position) {
        holder.bind(fields.get(position));
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    class OwnerFieldViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFieldName, tvFieldTypeBadge, tvFieldCapacity, tvFieldRating, tvStatusLabel;
        private final ImageView ivFieldImage;
        private final View llFieldRating;
        private final MaterialButton btnEditField, btnDeleteField, btnManageSlots;

        OwnerFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldTypeBadge = itemView.findViewById(R.id.tvFieldTypeBadge);
            tvFieldCapacity = itemView.findViewById(R.id.tvFieldCapacity);
            tvFieldRating = itemView.findViewById(R.id.tvFieldRating);
            ivFieldImage = itemView.findViewById(R.id.ivFieldImage);
            llFieldRating = itemView.findViewById(R.id.llFieldRating);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            btnEditField = itemView.findViewById(R.id.btnEditField);
            btnDeleteField = itemView.findViewById(R.id.btnDeleteField);
            btnManageSlots = itemView.findViewById(R.id.btnManageSlots);
        }

        void bind(Field field) {
            tvFieldName.setText(field.getName());
            tvFieldTypeBadge.setText(field.getTypeLabel());
            if (tvFieldCapacity != null) tvFieldCapacity.setText(field.getFieldType());

            Glide.with(itemView.getContext())
                    .load(field.getImageUrl())
                    .placeholder(R.drawable.bg_pitch_cover)
                    .error(R.drawable.bg_pitch_cover)
                    .centerCrop()
                    .into(ivFieldImage);

            Double rating = field.getAverageRating();
            if (llFieldRating != null) {
                if (rating != null && rating > 0) {
                    llFieldRating.setVisibility(View.VISIBLE);
                    tvFieldRating.setText(String.format(Locale.US, "%.1f", rating));
                } else {
                    llFieldRating.setVisibility(View.GONE);
                }
            }


            // Nhãn trạng thái đơn giản (Hoạt động/Bảo trì)
            String status = field.getStatus() == null ? "AVAILABLE" : field.getStatus();
            if ("MAINTENANCE".equalsIgnoreCase(status)) {
                tvStatusLabel.setText("Bảo trì");
                tvStatusLabel.setBackgroundResource(R.drawable.bg_status_maintenance);
                tvStatusLabel.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.orange_700));
            } else {
                tvStatusLabel.setText("Hoạt động");
                tvStatusLabel.setBackgroundResource(R.drawable.bg_status_available);
                tvStatusLabel.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green_primary));
            }

            btnEditField.setOnClickListener(v -> listener.onEditField(field));
            btnDeleteField.setOnClickListener(v -> listener.onDeleteField(field));
            btnManageSlots.setOnClickListener(v -> listener.onManageTimeSlots(field));
        }
    }
}
