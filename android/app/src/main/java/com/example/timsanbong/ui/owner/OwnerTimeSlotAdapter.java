package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.TimeSlotResponse;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerTimeSlotAdapter extends RecyclerView.Adapter<OwnerTimeSlotAdapter.OwnerTimeSlotViewHolder> {
    public interface Listener {
        void onEditTimeSlot(TimeSlotResponse timeSlot);
        void onDeleteTimeSlot(TimeSlotResponse timeSlot);
    }

    private final List<TimeSlotResponse> timeSlots = new ArrayList<>();
    private final Listener listener;

    public OwnerTimeSlotAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<TimeSlotResponse> newTimeSlots) {
        timeSlots.clear();
        if (newTimeSlots != null) {
            timeSlots.addAll(newTimeSlots);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OwnerTimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_time_slot, parent, false);
        return new OwnerTimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerTimeSlotViewHolder holder, int position) {
        holder.bind(timeSlots.get(position));
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    class OwnerTimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTimeRange;
        private final TextView tvPrice;
        private final TextView tvStatus;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        OwnerTimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimeRange = itemView.findViewById(R.id.tvOwnerSlotTimeRange);
            tvPrice = itemView.findViewById(R.id.tvOwnerSlotPrice);
            tvStatus = itemView.findViewById(R.id.tvOwnerSlotStatus);
            btnEdit = itemView.findViewById(R.id.btnEditOwnerSlot);
            btnDelete = itemView.findViewById(R.id.btnDeleteOwnerSlot);
        }

        void bind(TimeSlotResponse timeSlot) {
            tvTimeRange.setText(formatTime(timeSlot));
            tvPrice.setText(formatMoney(timeSlot.getPrice()));
            String status = timeSlot.isAvailable() ? "AVAILABLE" : safeStatus(timeSlot.getStatus());
            tvStatus.setText(labelForStatus(status));
            styleStatus(status);
            btnEdit.setOnClickListener(v -> listener.onEditTimeSlot(timeSlot));
            btnDelete.setOnClickListener(v -> listener.onDeleteTimeSlot(timeSlot));
        }

        private void styleStatus(String status) {
            int background;
            int textColor;
            switch (status) {
                case "AVAILABLE":
                    background = R.drawable.bg_tag_emerald;
                    textColor = R.color.green_primary;
                    break;
                case "PENDING":
                    background = R.drawable.bg_tag_amber;
                    textColor = R.color.orange_700;
                    break;
                case "BOOKED":
                    background = R.drawable.bg_status_cancelled;
                    textColor = R.color.red_600;
                    break;
                default:
                    background = R.drawable.bg_tag_emerald;
                    textColor = R.color.green_primary;
                    break;
            }
            tvStatus.setBackgroundResource(background);
            tvStatus.setTextColor(ContextCompat.getColor(tvStatus.getContext(), textColor));
        }

        private String formatTime(TimeSlotResponse timeSlot) {
            return safe(timeSlot.getStartTime()) + " - " + safe(timeSlot.getEndTime());
        }

        private String formatMoney(Double value) {
            if (value == null) {
                return "0 ₫";
            }
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " ₫";
        }

        private String safe(String value) {
            return value == null ? "--:--" : value;
        }

        private String safeStatus(String status) {
            return status == null ? "AVAILABLE" : status.toUpperCase(Locale.US);
        }

        private String labelForStatus(String status) {
            switch (status) {
                case "AVAILABLE":
                    return "Trống";
                case "PENDING":
                    return "Tạm giữ";
                case "BOOKED":
                    return "Đã đặt";
                default:
                    return status;
            }
        }
    }
}
