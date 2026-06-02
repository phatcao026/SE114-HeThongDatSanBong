package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnCancelClickListener {
        void onCancel(long bookingId);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final OnCancelClickListener listener;

    public BookingAdapter(List<Booking> bookings, OnCancelClickListener listener) {
        if (bookings != null) {
            this.bookings.addAll(bookings);
        }
        this.listener = listener;
    }

    public void updateBookings(List<Booking> newBookings) {
        bookings.clear();
        if (newBookings != null) {
            bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFieldName, tvDate, tvTime, tvTotalPrice, tvStatus;
        private final MaterialButton btnCancel;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(Booking booking) {
            if (booking.getField() != null) {
                tvFieldName.setText(booking.getField().getName());
            } else {
                tvFieldName.setText("-");
            }
            tvDate.setText(booking.getBookingDate());
            tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
            tvTotalPrice.setText(String.format("%,.0f đ", booking.getTotalPrice()));
            String status = booking.getStatus() != null ? booking.getStatus().toUpperCase(Locale.US) : "";
            tvStatus.setText(getStatusLabel(status));
            applyStatusStyle(status);

            boolean canCancel = "PENDING".equals(status) || "CONFIRMED".equals(status);
            btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> listener.onCancel(booking.getId()));
        }

        private String getStatusLabel(String status) {
            if ("PENDING".equals(status)) {
                return itemView.getContext().getString(R.string.status_pending);
            }
            if ("CONFIRMED".equals(status)) {
                return itemView.getContext().getString(R.string.status_confirmed);
            }
            if ("CANCELLED".equals(status) || "CANCELED".equals(status)) {
                return itemView.getContext().getString(R.string.status_cancelled);
            }
            return itemView.getContext().getString(R.string.status_unknown);
        }

        private void applyStatusStyle(String status) {
            int backgroundRes = R.color.status_default_bg;
            int textRes = R.color.text_secondary;
            if ("PENDING".equals(status)) {
                backgroundRes = R.color.status_pending_bg;
                textRes = R.color.warning;
            } else if ("CONFIRMED".equals(status)) {
                backgroundRes = R.color.status_confirmed_bg;
                textRes = R.color.success;
            } else if ("CANCELLED".equals(status) || "CANCELED".equals(status)) {
                backgroundRes = R.color.status_cancelled_bg;
                textRes = R.color.error;
            }
            ViewCompat.setBackgroundTintList(tvStatus,
                    ContextCompat.getColorStateList(itemView.getContext(), backgroundRes));
            tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), textRes));
        }
    }
}
