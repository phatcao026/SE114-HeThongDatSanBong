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

    public interface OnBookingActionListener {
        void onCancel(long bookingId);
        void onQrCheckin(Booking booking);
        void onDirections(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final OnBookingActionListener listener;

    public BookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_booking, parent, false);
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
        private final TextView tvFieldName, tvBookingRef, tvDate, tvTime, tvTotalPrice, tvStatus;
        private final TextView tvDepositFooter, tvRemainder;
        private final View layoutDepositFooter, layoutActions;
        private final MaterialButton btnQr, btnDirections, btnCancel;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvBookingRef = itemView.findViewById(R.id.tvBookingRef);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDepositFooter = itemView.findViewById(R.id.tvDepositFooter);
            tvRemainder = itemView.findViewById(R.id.tvRemainder);
            layoutDepositFooter = itemView.findViewById(R.id.layoutDepositFooter);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnQr = itemView.findViewById(R.id.btnQr);
            btnDirections = itemView.findViewById(R.id.btnDirections);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(Booking booking) {
            tvFieldName.setText(booking.getFieldName() != null ? booking.getFieldName() : "-");
            tvBookingRef.setText(String.format("#BK-%d", booking.getId()));
            tvDate.setText(formatDate(booking.getBookingDate()));
            tvTime.setText(formatTime(booking.getStartTime()) + " - " + formatTime(booking.getEndTime()));
            tvTotalPrice.setText(String.format("%,.0f đ", booking.getTotalPrice()));
            String status = booking.getStatus() != null ? booking.getStatus().toUpperCase(Locale.US) : "";
            tvStatus.setText(getStatusLabel(status));
            applyStatusStyle(status);

            boolean active = "PENDING".equals(status) || "DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status);
            layoutActions.setVisibility(active ? View.VISIBLE : View.GONE);
            if (active && booking.getDepositAmount() > 0) {
                layoutDepositFooter.setVisibility(View.VISIBLE);
                tvDepositFooter.setText(itemView.getContext().getString(
                        R.string.booking_deposit_footer,
                        String.format("%,.0f đ", booking.getDepositAmount())));
                tvRemainder.setText(String.format("%,.0f đ",
                        booking.getTotalAmount() - booking.getDepositAmount()));
            } else {
                layoutDepositFooter.setVisibility(View.GONE);
            }

            btnQr.setOnClickListener(v -> listener.onQrCheckin(booking));
            btnDirections.setOnClickListener(v -> listener.onDirections(booking));
            btnCancel.setOnClickListener(v -> listener.onCancel(booking.getId()));
        }

        /** "2026-06-11" → "11/06/2026" */
        private String formatDate(String date) {
            if (date == null) return "";
            String[] parts = date.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
            return date;
        }

        /** "18:00:00" → "18:00" */
        private String formatTime(String time) {
            if (time == null) return "";
            return time.length() >= 5 ? time.substring(0, 5) : time;
        }

        private String getStatusLabel(String status) {
            if ("PENDING".equals(status)) {
                return itemView.getContext().getString(R.string.status_pending);
            }
            if ("DEPOSIT_PAID".equals(status)) {
                return itemView.getContext().getString(R.string.status_deposit_paid);
            }
            if ("CONFIRMED".equals(status)) {
                return itemView.getContext().getString(R.string.status_confirmed);
            }
            if ("COMPLETED".equals(status)) {
                return itemView.getContext().getString(R.string.status_completed);
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
            } else if ("DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status)) {
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
