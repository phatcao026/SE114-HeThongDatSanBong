package com.example.fieldbooking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fieldbooking.R;
import com.example.fieldbooking.model.Booking;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnCancelClickListener {
        void onCancel(long bookingId);
    }

    private final List<Booking> bookings;
    private final OnCancelClickListener listener;

    public BookingAdapter(List<Booking> bookings, OnCancelClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
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
            }
            tvDate.setText(booking.getBookingDate());
            tvTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
            tvTotalPrice.setText(String.format("%,.0f đ", booking.getTotalPrice()));
            tvStatus.setText(booking.getStatus());

            boolean canCancel = "PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus());
            btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> listener.onCancel(booking.getId()));
        }
    }
}
