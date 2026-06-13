package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerBookingAdapter extends RecyclerView.Adapter<OwnerBookingAdapter.OwnerBookingViewHolder> {
    public interface Listener {
        void onConfirmBooking(Booking booking);
        void onCompleteBooking(Booking booking);
        void onCancelBooking(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final Listener listener;

    public OwnerBookingAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Booking> newBookings) {
        bookings.clear();
        if (newBookings != null) {
            bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OwnerBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_booking, parent, false);
        return new OwnerBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerBookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class OwnerBookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFieldName;
        private final TextView tvCustomerName;
        private final TextView tvBookingDate;
        private final TextView tvBookingTime;
        private final TextView tvBookingPrice;
        private final TextView tvBookingStatus;
        private final MaterialButton btnConfirm;
        private final MaterialButton btnComplete;
        private final MaterialButton btnCancel;

        OwnerBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvOwnerBookingFieldName);
            tvCustomerName = itemView.findViewById(R.id.tvOwnerBookingCustomer);
            tvBookingDate = itemView.findViewById(R.id.tvOwnerBookingDate);
            tvBookingTime = itemView.findViewById(R.id.tvOwnerBookingTime);
            tvBookingPrice = itemView.findViewById(R.id.tvOwnerBookingPrice);
            tvBookingStatus = itemView.findViewById(R.id.tvOwnerBookingStatus);
            btnConfirm = itemView.findViewById(R.id.btnConfirmOwnerBooking);
            btnComplete = itemView.findViewById(R.id.btnCompleteOwnerBooking);
            btnCancel = itemView.findViewById(R.id.btnCancelOwnerBooking);
        }

        void bind(Booking booking) {
            String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
            tvFieldName.setText(booking.getField() == null ? "Chua co san" : booking.getField().getName());
            tvCustomerName.setText(booking.getUser() == null ? "Chua co nguoi dat" : booking.getUser().getFullName());
            tvBookingDate.setText(booking.getBookingDate());
            tvBookingTime.setText(booking.getStartTime() + " - " + booking.getEndTime());
            tvBookingPrice.setText(String.format(Locale.US, "%,.0f d", booking.getTotalPrice()));
            tvBookingStatus.setText(status.isEmpty() ? "UNKNOWN" : status);

            boolean canConfirm = "PENDING".equals(status) || "DEPOSIT_PAID".equals(status);
            boolean canComplete = "CONFIRMED".equals(status);
            boolean canCancel = canConfirm || canComplete;

            btnConfirm.setVisibility(canConfirm ? View.VISIBLE : View.GONE);
            btnComplete.setVisibility(canComplete ? View.VISIBLE : View.GONE);
            btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

            btnConfirm.setOnClickListener(v -> listener.onConfirmBooking(booking));
            btnComplete.setOnClickListener(v -> listener.onCompleteBooking(booking));
            btnCancel.setOnClickListener(v -> listener.onCancelBooking(booking));
        }
    }
}
