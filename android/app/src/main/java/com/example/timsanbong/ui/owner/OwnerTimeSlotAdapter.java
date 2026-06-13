package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.TimeSlot;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerTimeSlotAdapter extends RecyclerView.Adapter<OwnerTimeSlotAdapter.OwnerTimeSlotViewHolder> {
    public interface Listener {
        void onEditTimeSlot(TimeSlot timeSlot);
        void onDeleteTimeSlot(TimeSlot timeSlot);
    }

    private final List<TimeSlot> timeSlots = new ArrayList<>();
    private final Listener listener;

    public OwnerTimeSlotAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<TimeSlot> newTimeSlots) {
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

        void bind(TimeSlot timeSlot) {
            tvTimeRange.setText(timeSlot.getStartTime() + " - " + timeSlot.getEndTime());
            tvPrice.setText(String.format(Locale.US, "%,.0f d", timeSlot.getPrice()));
            tvStatus.setText(timeSlot.getStatus() == null ? "UNKNOWN" : timeSlot.getStatus());
            btnEdit.setOnClickListener(v -> listener.onEditTimeSlot(timeSlot));
            btnDelete.setOnClickListener(v -> listener.onDeleteTimeSlot(timeSlot));
        }
    }
}
