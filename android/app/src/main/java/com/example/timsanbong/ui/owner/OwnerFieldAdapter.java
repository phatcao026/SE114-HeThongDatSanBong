package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.TimeSlotResponse;
import com.google.android.material.button.MaterialButton;

import android.util.Log;

import java.text.NumberFormat;
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
    
    // Add a diff util callback if list updates are frequent and large

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
        private final View viewAccentBar;
        private final TextView tvFieldName;
        private final TextView tvFieldTypeBadge;
        private final TextView tvFieldCapacity;
        private final TextView tvFieldRating;
        private final TextView tvStatusLabel;
        private final TextView tvFieldPrice;
        private final TextView tvBookedToday;
        private final TextView tvTotalSlotsLabel;
        private final TextView tvSlotSummary;
        private final View progressSlotUtil;
        private final MaterialButton btnEditField;
        private final MaterialButton btnDeleteField;
        private final MaterialButton btnManageSlots;

        OwnerFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAccentBar = itemView.findViewById(R.id.viewAccentBar);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldTypeBadge = itemView.findViewById(R.id.tvFieldTypeBadge);
            tvFieldCapacity = itemView.findViewById(R.id.tvFieldCapacity);
            tvFieldRating = itemView.findViewById(R.id.tvFieldRating);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            tvFieldPrice = itemView.findViewById(R.id.tvFieldPrice);
            tvBookedToday = itemView.findViewById(R.id.tvBookedToday);
            tvTotalSlotsLabel = itemView.findViewById(R.id.tvTotalSlotsLabel);
            tvSlotSummary = itemView.findViewById(R.id.tvSlotSummary);
            progressSlotUtil = itemView.findViewById(R.id.progressSlotUtil);
            btnEditField = itemView.findViewById(R.id.btnEditField);
            btnDeleteField = itemView.findViewById(R.id.btnDeleteField);
            btnManageSlots = itemView.findViewById(R.id.btnManageSlots);
        }

        void bind(Field field) {
            tvFieldName.setText(nonEmpty(field.getName(), "Chưa có tên sân"));
            tvFieldTypeBadge.setText(nonEmpty(field.getTypeLabel(), "Sân bóng"));
            tvFieldCapacity.setText(resolveCapacityLabel(field));
            tvFieldRating.setText(formatRating(field.getAverageRating()));
            tvStatusLabel.setText(nonEmpty(field.getStatus(), "AVAILABLE"));
            tvFieldPrice.setText(formatMoney(resolveMinimumPrice(field)));
            tvBookedToday.setText(String.valueOf(countBookedSlots(field)));
            tvTotalSlotsLabel.setText("/" + resolveSlotCount(field) + " slot");
            tvSlotSummary.setText(resolveSlotSummary(field));
            applyStatusStyle(field.getStatus());
            applyProgressStyle(field);

            btnEditField.setOnClickListener(v -> listener.onEditField(field));
            btnDeleteField.setOnClickListener(v -> listener.onDeleteField(field));
            btnManageSlots.setOnClickListener(v -> listener.onManageTimeSlots(field));
        }

        private void applyStatusStyle(String status) {
            String normalized = status == null ? "AVAILABLE" : status.toUpperCase(Locale.US);
            switch (normalized) {
                case "MAINTENANCE":
                    tvStatusLabel.setBackgroundResource(R.drawable.bg_status_maintenance);
                    tvStatusLabel.setTextColor(ContextCompat.getColor(tvStatusLabel.getContext(), R.color.orange_700));
                    break;
                case "BOOKED":
                    tvStatusLabel.setBackgroundResource(R.drawable.bg_status_cancelled);
                    tvStatusLabel.setTextColor(ContextCompat.getColor(tvStatusLabel.getContext(), R.color.red_600));
                    break;
                default:
                    tvStatusLabel.setBackgroundResource(R.drawable.bg_status_available);
                    tvStatusLabel.setTextColor(ContextCompat.getColor(tvStatusLabel.getContext(), R.color.green_primary));
                    break;
            }
        }

        private void applyProgressStyle(Field field) {
            int utilization = calculateUtilization(field);
            if (progressSlotUtil instanceof android.widget.ProgressBar) {
                ((android.widget.ProgressBar) progressSlotUtil).setProgress(utilization);
            }
        }

        private int calculateUtilization(Field field) {
            int total = resolveSlotCount(field);
            if (total == 0) {
                return 0;
            }
            int booked = countBookedSlots(field);
            return Math.min(100, Math.round((booked * 100f) / total));
        }

        private int resolveSlotCount(Field field) {
            return field.getTimeSlots() == null ? 0 : field.getTimeSlots().size();
        }

        private int countBookedSlots(Field field) {
            int booked = 0;
            if (field.getTimeSlots() != null) {
                for (TimeSlotResponse slot : field.getTimeSlots()) {
                    if (!slot.isAvailable()) {
                        booked++;
                    }
                }
            }
            return booked;
        }

        private String resolveSlotSummary(Field field) {
            int total = resolveSlotCount(field);
            int booked = countBookedSlots(field);
            int free = Math.max(0, total - booked);
            return free + " trống · " + booked + " đã đặt";
        }

        private String resolveCapacityLabel(Field field) {
            String type = field.getType() == null ? "" : field.getType().toUpperCase(Locale.US);
            if (type.contains("11")) {
                return "Sân 11";
            }
            if (type.contains("7")) {
                return "Sân 7";
            }
            if (type.contains("5")) {
                return "Sân 5";
            }
            return nonEmpty(field.getTypeLabel(), "Sân bóng");
        }

        private double resolveMinimumPrice(Field field) {
            double min = 0;
            if (field.getTimeSlots() != null) {
                for (TimeSlotResponse slot : field.getTimeSlots()) {
                    if (slot.getPrice() == null) {
                        continue;
                    }
                    if (min == 0 || slot.getPrice() < min) {
                        min = slot.getPrice();
                    }
                }
            }
            return min;
        }

        private String formatMoney(double value) {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " ₫";
        }

        private String formatRating(Double value) {
            if (value == null || value <= 0) {
                return "0.0";
            }
            return String.format(Locale.US, "%.1f", value);
        }

        private String nonEmpty(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
