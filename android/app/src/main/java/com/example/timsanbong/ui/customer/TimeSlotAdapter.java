package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<TimeSlot> timeSlots;
    private int selectedPosition = -1;
    private final OnTimeSlotSelectedListener listener;

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(TimeSlot timeSlot);
    }

    public static class TimeSlot {
        public long id;
        public String startTime;
        public String endTime;
        public boolean isAvailable;
        public double price;

        public TimeSlot(long id, String startTime, String endTime, boolean isAvailable, double price) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isAvailable = isAvailable;
            this.price = price;
        }

        public String timeRange() {
            return startTime + " - " + endTime;
        }
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlots, OnTimeSlotSelectedListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    public void updateSlots(List<TimeSlot> newSlots) {
        this.timeSlots = newSlots;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = timeSlots.get(position);
        holder.tvSlotTime.setText(slot.startTime);
        holder.tvSlotEnd.setText(holder.itemView.getContext().getString(R.string.time_range_arrow, slot.endTime));

        String priceText = slot.price > 0
                ? String.format("%,.0f %s", slot.price, holder.itemView.getContext().getString(R.string.currency_vnd))
                : holder.itemView.getContext().getString(R.string.available);

        if (!slot.isAvailable) {
            holder.itemView.setBackgroundResource(R.drawable.bg_time_slot_booked);
            holder.tvSlotTime.setTextColor(holder.itemView.getContext().getColor(R.color.slot_booked_text));
            holder.tvSlotEnd.setTextColor(holder.itemView.getContext().getColor(R.color.slot_booked_text));
            holder.tvSlotStatus.setTextColor(holder.itemView.getContext().getColor(R.color.slot_booked_text));
            holder.tvSlotStatus.setText(R.string.booked);
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.7f);
        } else if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_time_slot_selected);
            holder.tvSlotTime.setTextColor(holder.itemView.getContext().getColor(R.color.text_on_primary));
            holder.tvSlotEnd.setTextColor(holder.itemView.getContext().getColor(R.color.text_on_primary));
            holder.tvSlotStatus.setTextColor(holder.itemView.getContext().getColor(R.color.text_on_primary));
            holder.tvSlotStatus.setText(priceText);
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1f);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_time_slot_available);
            holder.tvSlotTime.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
            holder.tvSlotEnd.setTextColor(holder.itemView.getContext().getColor(R.color.text_hint));
            holder.tvSlotStatus.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
            holder.tvSlotStatus.setText(priceText);
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!slot.isAvailable) return;

            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (previous != -1) {
                notifyItemChanged(previous);
            }
            if (selectedPosition != -1) {
                notifyItemChanged(selectedPosition);
            }

            if (listener != null) {
                listener.onTimeSlotSelected(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlotTime;
        TextView tvSlotEnd;
        TextView tvSlotStatus;

        TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlotTime = itemView.findViewById(R.id.tvSlotTime);
            tvSlotEnd = itemView.findViewById(R.id.tvSlotEnd);
            tvSlotStatus = itemView.findViewById(R.id.tvSlotStatus);
        }
    }
}
