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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections; // Cần thêm import này
import java.util.List;
import java.util.Locale;

public class BookingCardAdapter extends RecyclerView.Adapter<BookingCardAdapter.ViewHolder> {
    public interface Listener {
        void onCheckIn(Booking booking);
        void onCollectRest(Booking booking);
        void onNoShow(Booking booking);
        void onConfirm(Booking booking);
        void onCancel(Booking booking);
        void onComplete(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final Listener listener;
    private boolean actionInProgress = false;
    private long busyBookingId = -1L;

    public BookingCardAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Booking> newList) {
        bookings.clear();
        if (newList != null) {
            List<Booking> sortedList = new ArrayList<>(newList);
            // Sắp xếp: Ưu tiên trạng thái hoạt động lên đầu, sau đó theo ID mới nhất
            Collections.sort(sortedList, (b1, b2) -> {
                int p1 = getStatusPriority(b1.getStatus());
                int p2 = getStatusPriority(b2.getStatus());
                if (p1 != p2) return Integer.compare(p1, p2);
                return Long.compare(b2.getId(), b1.getId());
            });
            bookings.addAll(sortedList);
        }
        notifyDataSetChanged();
    }

    // Hàm phụ để định nghĩa độ ưu tiên hiển thị
    private int getStatusPriority(String status) {
        if (status == null) return 5;
        switch (status.toUpperCase(Locale.US)) {
            case "DEPOSIT_PAID": return 0; // Cần check-in ngay
            case "CONFIRMED": return 1;    // Đang đá, chuẩn bị thu tiền
            case "PENDING": return 2;      // Chờ khách cọc
            case "COMPLETED": return 3;    // Đã xong
            case "CANCELLED": return 4;    // Đã hủy
            default: return 5;
        }
    }

    public void setActionState(boolean inProgress, long bookingId) {
        this.actionInProgress = inProgress;
        this.busyBookingId = bookingId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_booking_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvBookingStatusPill, tvBookingCustomerName, tvBookingPhone,
                tvBookingFieldValue, tvBookingFieldType, tvBookingTimeSlot, tvBookingDate, tvBookingPrice,
                btnCompleteBooking;
        View viewBookingAccentBar, viewBookingAvatarBg, viewPillDot;
        View llActionsPending, btnCancelBooking, btnConfirmBooking, llActionConfirmed, llStatusFooter;
        TextView tvStatusFooterMsg;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewBookingAccentBar = itemView.findViewById(R.id.viewBookingAccentBar);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvBookingStatusPill = itemView.findViewById(R.id.tvBookingStatusPill);
            viewPillDot = itemView.findViewById(R.id.viewPillDot);
            viewBookingAvatarBg = itemView.findViewById(R.id.viewBookingAvatarBg);
            tvBookingCustomerName = itemView.findViewById(R.id.tvBookingCustomerName);
            tvBookingPhone = itemView.findViewById(R.id.tvBookingPhone);
            tvBookingFieldValue = itemView.findViewById(R.id.tvBookingFieldValue);
            tvBookingFieldType = itemView.findViewById(R.id.tvBookingFieldType);
            tvBookingTimeSlot = itemView.findViewById(R.id.tvBookingTimeSlot);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvBookingPrice = itemView.findViewById(R.id.tvBookingPrice);

            llActionsPending = itemView.findViewById(R.id.llActionsPending);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            btnConfirmBooking = itemView.findViewById(R.id.btnConfirmBooking);
            llActionConfirmed = itemView.findViewById(R.id.llActionConfirmed);
            btnCompleteBooking = itemView.findViewById(R.id.btnCompleteBooking);
            llStatusFooter = itemView.findViewById(R.id.llStatusFooter);
            tvStatusFooterMsg = itemView.findViewById(R.id.tvStatusFooterMsg);
        }

        void bind(Booking booking) {
            tvBookingId.setText("#" + booking.getId());
            tvBookingCustomerName.setText(booking.getUser() != null ? booking.getUser().getFullName() : "Khách");
            tvBookingPhone.setText(booking.getUser() != null ? booking.getUser().getPhone() : "");
            tvBookingFieldValue.setText(booking.getFieldName());
            tvBookingFieldType.setText(booking.getField() != null ? booking.getField().getTypeLabel() : "");
            tvBookingTimeSlot.setText(formatTimeRange(booking.getStartTime(), booking.getEndTime()));
            tvBookingDate.setText(booking.getBookingDate());

            double total = booking.getTotalAmount();
            double deposit = booking.getDepositAmount();
            String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);

            // Xử lý độ mờ cho đơn đã kết thúc
            if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }

            // Hiển thị tiền theo trạng thái CONFIRMED (70%) hoặc các loại khác (100%)
            if ("CONFIRMED".equals(status)) {
                double remaining = total - deposit;
                tvBookingPrice.setText("Còn lại: " + formatMoney(remaining));
                tvBookingPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blue_600));
            } else {
                tvBookingPrice.setText(formatMoney(total));
                tvBookingPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.emerald_600));
            }

            styleStatus(status);

            llActionsPending.setVisibility(View.GONE);
            llActionConfirmed.setVisibility(View.GONE);
            llStatusFooter.setVisibility(View.GONE);

            boolean isBusy = actionInProgress && booking.getId() == busyBookingId;

            switch (status) {
                case "PENDING":
                    llActionsPending.setVisibility(View.VISIBLE);
                    setPrimaryButton(btnConfirmBooking, "Xác nhận", v -> {
                        if (listener != null) listener.onConfirm(booking);
                    }, isBusy);
                    setSecondaryButton(btnCancelBooking, "Hủy đặt sân", v -> {
                        if (listener != null) listener.onCancel(booking);
                    }, isBusy);
                    break;
                case "DEPOSIT_PAID":
                    llActionsPending.setVisibility(View.VISIBLE);
                    setPrimaryButton(btnConfirmBooking, "Check-in", v -> {
                        if (listener != null) listener.onCheckIn(booking);
                    }, isBusy);
                    setSecondaryButton(btnCancelBooking, "No-show", v -> {
                        if (listener != null) listener.onNoShow(booking);
                    }, isBusy);
                    break;
                case "CONFIRMED":
                    llActionConfirmed.setVisibility(View.VISIBLE);
                    btnCompleteBooking.setText("Check-out (Thu nốt tiền)");
                    btnCompleteBooking.setOnClickListener(v -> {
                        if (listener != null) listener.onCollectRest(booking);
                    });
                    btnCompleteBooking.setEnabled(!isBusy);
                    break;
                case "COMPLETED":
                    llStatusFooter.setVisibility(View.VISIBLE);
                    if (tvStatusFooterMsg != null) tvStatusFooterMsg.setText("Đã hoàn thành");
                    break;
                case "CANCELLED":
                    llStatusFooter.setVisibility(View.VISIBLE);
                    if (tvStatusFooterMsg != null) tvStatusFooterMsg.setText("Đã hủy");
                    break;
                default:
                    llStatusFooter.setVisibility(View.VISIBLE);
                    if (tvStatusFooterMsg != null) tvStatusFooterMsg.setText(status.isEmpty() ? "—" : status);
                    break;
            }
        }

        private void setPrimaryButton(View btnView, String text, View.OnClickListener listenerClick, boolean disabled) {
            if (btnView == null) return;
            TextView label = findFirstTextView(btnView);
            if (label != null) label.setText(text);
            btnView.setOnClickListener(listenerClick);
            btnView.setEnabled(!disabled);
        }

        private void setSecondaryButton(View btnView, String text, View.OnClickListener listenerClick, boolean disabled) {
            setPrimaryButton(btnView, text, listenerClick, disabled);
        }

        private TextView findFirstTextView(View root) {
            if (root instanceof TextView) return (TextView) root;
            if (!(root instanceof ViewGroup)) return null;
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) return (TextView) child;
            }
            return null;
        }

        private void styleStatus(String status) {
            int background = R.drawable.bg_status_pending;
            int dot = R.drawable.bg_badge_orange;
            int textColor = R.color.amber_600;
            String label = "Pending";
            switch (status) {
                case "PENDING": label = "Chờ cọc"; background = R.drawable.bg_status_pending; dot = R.drawable.bg_badge_orange; textColor = R.color.amber_600; break;
                case "DEPOSIT_PAID": label = "Đã cọc"; background = R.drawable.bg_status_confirmed; dot = R.drawable.bg_badge_green; textColor = R.color.green_primary; break;
                case "CONFIRMED": label = "Đã xác nhận"; background = R.drawable.bg_status_confirmed; dot = R.drawable.bg_badge_green; textColor = R.color.green_primary; break;
                case "COMPLETED": label = "Hoàn thành"; background = R.drawable.bg_status_completed; dot = R.drawable.bg_badge_green; textColor = R.color.green_primary; break;
                case "CANCELLED": label = "Đã hủy"; background = R.drawable.bg_status_cancelled; dot = R.drawable.bg_badge_red; textColor = R.color.red_600; break;
                default: label = status.isEmpty() ? "—" : status; background = R.drawable.bg_status_pending; dot = R.drawable.bg_badge_orange; textColor = R.color.slate_400; break;
            }
            tvBookingStatusPill.setText(label);
            tvBookingStatusPill.setTextColor(ContextCompat.getColor(tvBookingStatusPill.getContext(), textColor));
            viewPillDot.setBackgroundResource(dot);
            View container = itemView.findViewById(R.id.containerStatusPill);
            if (container != null) container.setBackgroundResource(background);
        }

        private String formatTimeRange(String start, String end) {
            if (start == null) start = "--:--";
            if (end == null) end = "--:--";
            return start + " – " + end;
        }

        private String formatMoney(double amount) {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " ₫";
        }
    }
}