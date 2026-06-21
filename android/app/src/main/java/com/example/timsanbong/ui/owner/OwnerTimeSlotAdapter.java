package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageButton;
import com.example.timsanbong.R;
import com.example.timsanbong.data.model.TimeSlotResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
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
            List<TimeSlotResponse> sorted = new ArrayList<>(newTimeSlots);
            // Sắp xếp theo thời gian bắt đầu tăng dần
            Collections.sort(sorted, (o1, o2) -> {
                String s1 = o1.getStartTime() == null ? "" : o1.getStartTime();
                String s2 = o2.getStartTime() == null ? "" : o2.getStartTime();
                return s1.compareTo(s2);
            });
            timeSlots.addAll(sorted);
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
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

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
            
            // Logic làm mờ khung giờ đã đặt
            boolean available = timeSlot.isAvailable();
            if (available) {
                itemView.setAlpha(1.0f);
                btnEdit.setEnabled(true);
            } else {
                itemView.setAlpha(0.4f); // Làm mờ các khung giờ đã đặt hoặc không khả dụng
                btnEdit.setEnabled(false); // Thường khung giờ đã đặt sẽ không cho sửa trực tiếp ở đây
            }

            String status = timeSlot.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = available ? "AVAILABLE" : "BOOKED";
            } else {
                status = status.toUpperCase(Locale.US);
            }
            
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
            if (value == null) return "0 ₫";
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " ₫";
        }

        private String safe(String value) {
            return value == null ? "--:--" : value;
        }

        private String labelForStatus(String status) {
            switch (status) {
                case "AVAILABLE": return "Trống";
                case "PENDING": return "Tạm giữ";
                case "BOOKED": return "Đã đặt";
                default: return status;
            }
        }
    }
}
