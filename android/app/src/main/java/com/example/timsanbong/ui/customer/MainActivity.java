package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.ui.profile.ProfileActivity;
import com.example.timsanbong.ui.profile.ProfileViewModel;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.PushNotificationManager;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvAvatar;
    private TextView tvGreeting;
    private TextView tvUpcomingCount;
    private TextView tvTrustScore;
    private TextView tvNotificationCount;
    private TextView tvUpcomingFieldName;
    private TextView tvUpcomingTime;
    private TextView tvUpcomingStatus;
    private TextView tvPaymentReminderTitle;
    private TextView tvPaymentReminderBody;
    private TextView tvUpcomingSeeAll;
    private TextView tvSuggestedMore;
    private View viewNotificationDot;
    private MaterialCardView btnNotifications;
    private MaterialCardView cardNextBooking;
    private MaterialCardView cardPaymentReminder;
    private MaterialButton btnSearchNearby;
    private MaterialButton btnHomeBookings;
    private MaterialButton btnPaymentReminder;

    private RecyclerView rvSuggestedFields;

    private FieldViewModel fieldViewModel;
    private BookingViewModel bookingViewModel;
    private NotificationViewModel notificationViewModel;
    private ProfileViewModel profileViewModel;
    private SessionManager sessionManager;
    private Booking pendingPaymentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);
        PushNotificationManager.prepareForAuthenticatedUser(this);
        initViews();
        setupListeners();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookingViewModel != null) {
            bookingViewModel.loadMyBookings();
        }
        if (notificationViewModel != null) {
            notificationViewModel.loadUnreadCount();
        }
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        tvNotificationCount = findViewById(R.id.tvNotificationCount);
        tvUpcomingFieldName = findViewById(R.id.tvUpcomingFieldName);
        tvUpcomingTime = findViewById(R.id.tvUpcomingTime);
        tvUpcomingStatus = findViewById(R.id.tvUpcomingStatus);
        tvPaymentReminderTitle = findViewById(R.id.tvPaymentReminderTitle);
        tvPaymentReminderBody = findViewById(R.id.tvPaymentReminderBody);
        tvUpcomingSeeAll = findViewById(R.id.tvUpcomingSeeAll);
        tvSuggestedMore = findViewById(R.id.tvSuggestedMore);
        viewNotificationDot = findViewById(R.id.viewNotificationDot);
        btnNotifications = findViewById(R.id.btnNotifications);
        cardNextBooking = findViewById(R.id.cardNextBooking);
        cardPaymentReminder = findViewById(R.id.cardPaymentReminder);
        btnSearchNearby = findViewById(R.id.btnSearchNearby);
        btnHomeBookings = findViewById(R.id.btnHomeBookings);
        btnPaymentReminder = findViewById(R.id.btnPaymentReminder);

        rvSuggestedFields = findViewById(R.id.rvSuggestedFields);

        rvSuggestedFields.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        new NavBarManager(this, NavBarManager.ITEM_HOME).setup();
    }

    private void setupListeners() {
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, MessagesActivity.class)));
        tvAvatar.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
        btnSearchNearby.setOnClickListener(v -> openFindPitch());

        tvSuggestedMore.setOnClickListener(v -> openFindPitch());
        btnHomeBookings.setOnClickListener(v -> openBookings());
        tvUpcomingSeeAll.setOnClickListener(v -> openBookings());
        cardNextBooking.setOnClickListener(v -> openBookings());
        cardPaymentReminder.setOnClickListener(v -> openPendingPayment());
        btnPaymentReminder.setOnClickListener(v -> openPendingPayment());
    }

    private void loadData() {
        sessionManager = new SessionManager(this);
        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        bindSessionIdentity();
        bindEmptyDashboard();
        observeProfile();
        observeBookings();
        observeNotifications();
        observeFields();

        profileViewModel.loadProfile();
        bookingViewModel.loadMyBookings();
        notificationViewModel.loadUnreadCount();
        fieldViewModel.loadFields();
    }

    private void bindSessionIdentity() {
        try {
            JSONObject userJson = new JSONObject(sessionManager.getUserJson());
            String fullName = userJson.optString("fullName", "User");
            bindAvatarAndGreeting(fullName);
        } catch (Exception e) {
            bindAvatarAndGreeting("User");
        }
    }

    private void bindAvatarAndGreeting(String fullName) {
        String safeName = fullName == null || fullName.trim().isEmpty() ? "User" : fullName.trim();
        tvAvatar.setText(getInitials(safeName));
        tvGreeting.setText(getString(R.string.home_greeting_name, safeName));
    }

    private void bindEmptyDashboard() {
        tvUpcomingCount.setText("0");
        tvTrustScore.setText("0");
        tvNotificationCount.setText("0");
        viewNotificationDot.setVisibility(View.GONE);
        bindNextBooking(null);
        bindPaymentReminder(null);
    }

    private void observeProfile() {
        profileViewModel.profileState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS
                    || resource.data == null) {
                return;
            }
            bindAvatarAndGreeting(resource.data.getFullName());
            tvTrustScore.setText(String.valueOf(resource.data.getTrustScore()));
        });
    }

    private void observeBookings() {
        bookingViewModel.bookingsState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS
                    || resource.data == null) {
                return;
            }

            List<Booking> upcoming = new ArrayList<>();
            pendingPaymentBooking = null;
            for (Booking booking : resource.data) {
                String status = getStatus(booking);
                if (isUpcomingBooking(booking)) {
                    upcoming.add(booking);
                }
                if (pendingPaymentBooking == null && "PENDING".equals(status)) {
                    pendingPaymentBooking = booking;
                }
            }

            Collections.sort(upcoming, (a, b) -> Long.compare(getBookingDateMillis(a), getBookingDateMillis(b)));
            tvUpcomingCount.setText(String.valueOf(upcoming.size()));
            bindNextBooking(upcoming.isEmpty() ? null : upcoming.get(0));
            bindPaymentReminder(pendingPaymentBooking);
        });
    }

    private void observeNotifications() {
        notificationViewModel.unreadCountState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS
                    || resource.data == null) {
                return;
            }
            int count = resource.data;
            tvNotificationCount.setText(String.valueOf(count));
            viewNotificationDot.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void observeFields() {
        fieldViewModel.filteredFields.observe(this, fields -> {
            if (fields == null) return;
            List<SuggestedFieldItem> suggestedFields = new ArrayList<>();
            for (Field field : fields) {
                if (!field.isAvailable()) {
                    continue;
                }
                float rating = field.getAverageRating() != null ? field.getAverageRating().floatValue() : 0f;
                suggestedFields.add(new SuggestedFieldItem(
                        field.getId(),
                        field.getName(),
                        field.getImageUrl(),
                        field.getFieldType() != null ? field.getFieldType() : "San 5",
                        rating,
                        "",
                        true
                ));
                if (suggestedFields.size() == 8) {
                    break;
                }
            }
            SuggestedFieldAdapter suggestedFieldAdapter = new SuggestedFieldAdapter(suggestedFields, item -> {
                Intent intent = new Intent(MainActivity.this, FieldDetailActivity.class);
                intent.putExtra("fieldId", item.getId());
                startActivity(intent);
            });
            rvSuggestedFields.setAdapter(suggestedFieldAdapter);
        });
    }

    private void bindNextBooking(Booking booking) {
        if (booking == null) {
            tvUpcomingFieldName.setText(R.string.home_next_booking_empty);
            tvUpcomingTime.setText(R.string.home_next_booking_empty_time);
            tvUpcomingStatus.setText(R.string.action_book);
            return;
        }
        tvUpcomingFieldName.setText(booking.getFieldName());
        tvUpcomingTime.setText(formatBookingTime(booking));
        tvUpcomingStatus.setText(getStatusLabel(getStatus(booking)));
    }

    private void bindPaymentReminder(Booking booking) {
        boolean hasPending = booking != null;
        btnPaymentReminder.setVisibility(hasPending ? View.VISIBLE : View.GONE);
        cardPaymentReminder.setClickable(hasPending);
        if (hasPending) {
            tvPaymentReminderTitle.setText(R.string.home_payment_due_title);
            double amount = booking.getDepositAmount() > 0 ? booking.getDepositAmount() : booking.getTotalAmount();
            tvPaymentReminderBody.setText(getString(R.string.home_payment_due_body,
                    booking.getFieldName(), formatCurrency(amount)));
        } else {
            tvPaymentReminderTitle.setText(R.string.home_payment_clear_title);
            tvPaymentReminderBody.setText(R.string.home_payment_clear_body);
        }
    }

    private void openFindPitch() {
        startActivity(new Intent(this, FindPitchActivity.class));
    }

    private void openBookings() {
        startActivity(new Intent(this, MyBookingsActivity.class));
    }

    private void openPendingPayment() {
        if (pendingPaymentBooking == null) {
            return;
        }
        double dueAmount = pendingPaymentBooking.getDepositAmount() > 0
                ? pendingPaymentBooking.getDepositAmount()
                : pendingPaymentBooking.getTotalAmount();
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, pendingPaymentBooking.getId());
        intent.putExtra(Constants.EXTRA_PAYMENT_FIELD_NAME, pendingPaymentBooking.getFieldName());
        intent.putExtra(Constants.EXTRA_TOTAL_PRICE, pendingPaymentBooking.getTotalAmount());
        intent.putExtra(Constants.EXTRA_DEPOSIT_AMOUNT, dueAmount);
        intent.putExtra(Constants.EXTRA_REMAINDER_AMOUNT,
                pendingPaymentBooking.getTotalAmount() - dueAmount);
        startActivity(intent);
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.US);
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.US);
    }

    private boolean isUpcomingBooking(Booking booking) {
        String status = getStatus(booking);
        return "PENDING".equals(status) || "DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status);
    }

    private String getStatus(Booking booking) {
        return booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
    }

    private String getStatusLabel(String status) {
        if ("PENDING".equals(status)) {
            return getString(R.string.status_pending);
        }
        if ("DEPOSIT_PAID".equals(status)) {
            return getString(R.string.status_deposit_paid);
        }
        if ("CONFIRMED".equals(status)) {
            return getString(R.string.status_confirmed);
        }
        return getString(R.string.status_unknown);
    }

    private String formatBookingTime(Booking booking) {
        String date = booking.getBookingDate() != null ? formatDate(booking.getBookingDate()) : "";
        String start = formatTime(booking.getStartTime());
        String end = formatTime(booking.getEndTime());
        if (start.isEmpty() && end.isEmpty()) {
            return date;
        }
        return date + " " + start + "-" + end;
    }

    private String formatDate(String date) {
        String[] parts = date.split("-");
        if (parts.length == 3) {
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return date;
    }

    private String formatTime(String time) {
        if (time == null) return "";
        return time.length() >= 5 ? time.substring(0, 5) : time;
    }

    private long getBookingDateMillis(Booking booking) {
        if (booking == null || booking.getBookingDate() == null) {
            return 0;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.util.Date parsed = format.parse(booking.getBookingDate());
            return parsed != null ? parsed.getTime() : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f %s", amount, getString(R.string.currency_vnd));
    }
}
