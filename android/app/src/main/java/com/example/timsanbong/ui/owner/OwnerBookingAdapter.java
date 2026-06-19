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
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerBookingAdapter extends RecyclerView.Adapter<OwnerBookingAdapter.OwnerBookingViewHolder> {
    public interface Listener {
        void onCheckIn(Booking booking);
        void onCollectRest(Booking booking);
        void onNoShow(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final Listener listener;
    private boolean actionInProgress;
    private long busyBookingId = -1L;

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

    public void setActionState(boolean inProgress, long bookingId) {
        actionInProgress = inProgress;
        busyBookingId = bookingId;
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
        private final TextView tvOrderId;
        private final TextView tvFieldName;
        private final TextView tvStatus;
        private final TextView tvDate;
        private final TextView tvTime;
        private final TextView tvTotal;
        private final TextView tvDeposit;
        private final TextView tvNote;
        private final MaterialButton btnCheckIn;
        private final MaterialButton btnCollectRest;
        private final MaterialButton btnNoShow;

        OwnerBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvFieldName = itemView.findViewById(R.id.tv_field_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvDeposit = itemView.findViewById(R.id.tv_deposit);
            tvNote = itemView.findViewById(R.id.tv_note);
            btnCheckIn = itemView.findViewById(R.id.btn_confirm);
            btnCollectRest = itemView.findViewById(R.id.btn_checkin);
            btnNoShow = itemView.findViewById(R.id.btn_reject);
        }

        void bind(Booking booking) {
            String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
            tvOrderId.setText("#" + booking.getId());
            tvFieldName.setText(nonEmpty(booking.getFieldName(), "Sân bóng"));
            tvStatus.setText(labelForStatus(status));
            tvDate.setText(nonEmpty(booking.getBookingDate(), "—"));
            tvTime.setText(formatTimeRange(booking.getStartTime(), booking.getEndTime()));
            tvTotal.setText(formatMoney(booking.getTotalAmount()));
            tvDeposit.setText(formatMoney(booking.getDepositAmount()));
            tvNote.setText(nonEmpty(booking.getNote(), "Không có ghi chú"));

            styleStatus(tvStatus, status);

            boolean isDepositPaid = "DEPOSIT_PAID".equals(status);
            boolean isConfirmed = "CONFIRMED".equals(status);
            boolean canAct = isDepositPaid || isConfirmed;
            boolean isBusy = actionInProgress && booking.getId() == busyBookingId;

            btnCheckIn.setVisibility(canAct ? View.VISIBLE : View.GONE);
            btnCollectRest.setVisibility(canAct ? View.VISIBLE : View.GONE);
            btnNoShow.setVisibility(canAct ? View.VISIBLE : View.GONE);

            btnCheckIn.setText("Check-in");
            btnCollectRest.setText("Thu nốt");
            btnNoShow.setText("No-show");

            btnCheckIn.setEnabled(!isBusy);
            btnCollectRest.setEnabled(!isBusy);
            btnNoShow.setEnabled(!isBusy);

            btnCheckIn.setOnClickListener(v -> listener.onCheckIn(booking));
            btnCollectRest.setOnClickListener(v -> listener.onCollectRest(booking));
            btnNoShow.setOnClickListener(v -> listener.onNoShow(booking));
        }

        private void styleStatus(TextView statusView, String status) {
            int background;
            int textColor;
            String label;
            switch (status) {
                case "PENDING":
                    background = R.drawable.bg_tag_amber;
                    textColor = R.color.orange_700;
                    label = "Chờ cọc";
                    break;
                case "DEPOSIT_PAID":
                    background = R.drawable.bg_tag_emerald;
                    textColor = R.color.green_primary;
                    label = "Đã cọc";
                    break;
                case "CONFIRMED":
                    background = R.drawable.bg_tag_emerald;
                    textColor = R.color.green_primary;
                    label = "Đã xác nhận";
                    break;
                case "COMPLETED":
                    background = R.drawable.bg_tag_emerald;
                    textColor = R.color.green_primary;
                    label = "Hoàn thành";
                    break;
                case "CANCELLED":
                    background = R.drawable.bg_badge_red;
                    textColor = R.color.red_600;
                    label = "Đã hủy";
                    break;
                default:
                    background = R.drawable.bg_badge_neutral;
                    textColor = R.color.slate_500;
                    label = status.isEmpty() ? "Không rõ" : status;
                    break;
            }
            statusView.setBackgroundResource(background);
            statusView.setTextColor(ContextCompat.getColor(statusView.getContext(), textColor));
            statusView.setText(label);
        }

        private String labelForStatus(String status) {
            switch (status) {
                case "PENDING":
                    return "Chờ cọc";
                case "DEPOSIT_PAID":
                    return "Đã cọc";
                case "CONFIRMED":
                    return "Đã xác nhận";
                case "COMPLETED":
                    return "Hoàn thành";
                case "CANCELLED":
                    return "Đã hủy";
                default:
                    return "Không rõ";
            }
        }

        private String formatTimeRange(String start, String end) {
            return nonEmpty(start, "--:--") + " - " + nonEmpty(end, "--:--");
        }

        private String formatMoney(Double amount) {
            if (amount == null) {
                return "0 ₫";
            }
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " ₫";
        }

        private String formatMoney(BigDecimal amount) {
            if (amount == null) {
                return "0 ₫";
            }
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " ₫";
        }

        private String nonEmpty(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
