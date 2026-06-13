package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;

import java.util.List;

public class DateChipAdapter extends RecyclerView.Adapter<DateChipAdapter.DateViewHolder> {

    private List<DateChip> dateChips;
    private int selectedPosition = 0;
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(DateChip dateChip);
    }

    public static class DateChip {
        public String dayOfWeek;
        public String dayOfMonth;
        public String fullDate; // For API e.g. 2026-05-23

        public DateChip(String dayOfWeek, String dayOfMonth, String fullDate) {
            this.dayOfWeek = dayOfWeek;
            this.dayOfMonth = dayOfMonth;
            this.fullDate = fullDate;
        }
    }

    public DateChipAdapter(List<DateChip> dateChips, OnDateSelectedListener listener) {
        this.dateChips = dateChips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_date_chip, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateChip chip = dateChips.get(position);
        holder.tvDayOfWeek.setText(chip.dayOfWeek);
        holder.tvDayOfMonth.setText(chip.dayOfMonth);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_date_chip_selected);
            holder.tvDayOfWeek.setTextColor(holder.itemView.getContext().getColor(R.color.text_on_primary));
            holder.tvDayOfMonth.setTextColor(holder.itemView.getContext().getColor(R.color.text_on_primary));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_date_chip);
            holder.tvDayOfWeek.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
            holder.tvDayOfMonth.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onDateSelected(chip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateChips != null ? dateChips.size() : 0;
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfWeek;
        TextView tvDayOfMonth;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);
        }
    }
}
