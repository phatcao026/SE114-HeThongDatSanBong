package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentBookingAdapter extends RecyclerView.Adapter<RecentBookingAdapter.ViewHolder> {
    public interface Listener {
        void onItemClick(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final Listener listener;

    public RecentBookingAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Booking> newBookings) {
        bookings.clear();
        if (newBookings != null) bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_recent_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatarLetter;
        private final View viewAvatarBg;
        private final TextView tvCustomerName;
        private final TextView tvFieldAndSlot;
        private final TextView tvStatusPill;
        private final View viewStatusDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tvAvatarLetter);
            viewAvatarBg = itemView.findViewById(R.id.viewAvatarBg);
            tvCustomerName = itemView.findViewById(R.id.tvRecentCustomerName);
            tvFieldAndSlot = itemView.findViewById(R.id.tvRecentFieldAndSlot);
            tvStatusPill = itemView.findViewById(R.id.tvRecentStatusPill);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
        }

        void bind(Booking booking) {
            String customer = "Khách";
            User user = booking.getUser();
            if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                customer = user.getFullName();
            }
            // Avatar letter: last word first char
            String[] parts = customer.trim().split("\\s+");
            String letter = parts.length == 0 ? "K" : parts[parts.length - 1].substring(0, 1).toUpperCase(Locale.US);
            tvAvatarLetter.setText(letter);

            tvCustomerName.setText(customer);

            String fieldName = booking.getFieldName();
            String fieldType = "";
            if (booking.getField() != null) {
                fieldType = booking.getField().getTypeLabel();
            }
            String timeRange = formatTimeRange(booking.getStartTime(), booking.getEndTime());
            String fieldAndSlot = fieldName + (fieldType.isEmpty() ? "" : " · " + fieldType) + " · " + timeRange;
            tvFieldAndSlot.setText(fieldAndSlot);

            String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
            styleStatus(status);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(booking);
            });
        }

        private void styleStatus(String status) {
            int bg = R.drawable.bg_status_pending;
            int dotBg = R.drawable.bg_badge_orange;
            int textColor = R.color.amber_600;
            String label = "Pending";
            switch (status) {
                case "PENDING": label = "Chờ cọc"; bg = R.drawable.bg_status_pending; dotBg = R.drawable.bg_badge_orange; textColor = R.color.amber_600; break;
                case "DEPOSIT_PAID": label = "Đã cọc"; bg = R.drawable.bg_status_confirmed; dotBg = R.drawable.bg_badge_green; textColor = R.color.green_primary; break;
                case "CONFIRMED": label = "Xác nhận"; bg = R.drawable.bg_status_confirmed; dotBg = R.drawable.bg_badge_green; textColor = R.color.green_primary; break;
                case "COMPLETED": label = "Hoàn thành"; bg = R.drawable.bg_status_completed; dotBg = R.drawable.bg_badge_green; textColor = R.color.green_primary; break;
                case "CANCELLED": label = "Đã hủy"; bg = R.drawable.bg_status_cancelled; dotBg = R.drawable.bg_badge_red; textColor = R.color.red_600; break;
                default: label = status.isEmpty() ? "—" : status; bg = R.drawable.bg_status_pending; dotBg = R.drawable.bg_badge_orange; textColor = R.color.slate_400; break;
            }
            tvStatusPill.setText(label);
            tvStatusPill.setTextColor(ContextCompat.getColor(tvStatusPill.getContext(), textColor));
            // Backgrounds applied on container; tvStatusPill sits inside a container with id containerRecentStatusPill
            viewStatusDot.setBackgroundResource(dotBg);
            // set container background if present
            View container = itemView.findViewById(R.id.containerRecentStatusPill);
            if (container != null) container.setBackgroundResource(bg);
        }

        private String formatTimeRange(String start, String end) {
            if (start == null) start = "--:--";
            if (end == null) end = "--:--";
            return start + " – " + end;
        }
    }
}

