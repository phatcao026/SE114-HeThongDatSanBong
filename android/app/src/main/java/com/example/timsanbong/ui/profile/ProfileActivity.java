package com.example.timsanbong.ui.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.User;
import com.example.timsanbong.ui.auth.LoginActivity;
import com.example.timsanbong.ui.customer.BookingAdapter;
import com.example.timsanbong.ui.customer.BookingViewModel;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements BookingAdapter.OnBookingActionListener {

    private TextView tvAvatar, tvName, tvEmail, tvTrustScore;
    private android.widget.ProgressBar pbTrustScore;
    private TextView tabPersonalInfo, tabBookingHistory;
    private View cardPersonalInfo, layoutBookingHistory;
    private TextView tvEmptyBookings;
    private TextInputLayout tilName, tilPhone;
    private MaterialButton btnSaveProfile, btnLogout;
    private ProfileViewModel profileViewModel;
    private BookingViewModel bookingViewModel;
    private BookingAdapter bookingAdapter;
    private User currentUser;
    private long pendingCancelBookingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);

        initViews();
        setupTabs();
        setupListeners();
        setupObservers();

        new NavBarManager(this, NavBarManager.ITEM_PROFILE).setup();

        profileViewModel.loadProfile();
        bookingViewModel.loadMyBookings();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        pbTrustScore = findViewById(R.id.pbTrustScore);
        tabPersonalInfo = findViewById(R.id.tabPersonalInfo);
        tabBookingHistory = findViewById(R.id.tabBookingHistory);
        cardPersonalInfo = findViewById(R.id.cardPersonalInfo);
        layoutBookingHistory = findViewById(R.id.layoutBookingHistory);
        tvEmptyBookings = findViewById(R.id.tvEmptyBookings);
        tilName = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        View btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setVisibility(View.GONE);
        }

        RecyclerView rvBookingHistory = findViewById(R.id.rvBookingHistory);
        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter(new ArrayList<>(), this);
        rvBookingHistory.setAdapter(bookingAdapter);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> profileViewModel.logout());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void setupObservers() {
        profileViewModel.profileState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                bindProfile(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.profileUpdateState.observe(this, resource -> {
            if (resource == null) return;
            btnSaveProfile.setEnabled(resource.status != Resource.Status.LOADING);
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.logoutEvent.observe(this, shouldLogout -> {
            if (Boolean.TRUE.equals(shouldLogout)) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        bookingViewModel.bookingsState.observe(this, resource -> {
            if (resource == null || resource.status != Resource.Status.SUCCESS) {
                return;
            }
            List<Booking> bookings = resource.data == null ? new ArrayList<>() : resource.data;
            bookingAdapter.updateBookings(bookings);
            tvEmptyBookings.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
        });

        bookingViewModel.cancelState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS) {
                pendingCancelBookingId = -1;
                Toast.makeText(this, R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
                bookingViewModel.loadMyBookings();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTabs() {
        tabPersonalInfo.setOnClickListener(v -> showPersonalInfo());
        tabBookingHistory.setOnClickListener(v -> showBookingHistory());
    }

    private void showPersonalInfo() {
        tabPersonalInfo.setBackgroundResource(R.drawable.bg_segment_active);
        tabPersonalInfo.setTextColor(getColor(R.color.text_on_primary));
        tabPersonalInfo.setTypeface(null, android.graphics.Typeface.BOLD);

        tabBookingHistory.setBackgroundResource(android.R.color.transparent);
        tabBookingHistory.setTextColor(getColor(R.color.text_secondary));
        tabBookingHistory.setTypeface(null, android.graphics.Typeface.NORMAL);

        cardPersonalInfo.setVisibility(View.VISIBLE);
        layoutBookingHistory.setVisibility(View.GONE);
    }

    private void showBookingHistory() {
        tabBookingHistory.setBackgroundResource(R.drawable.bg_segment_active);
        tabBookingHistory.setTextColor(getColor(R.color.text_on_primary));
        tabBookingHistory.setTypeface(null, android.graphics.Typeface.BOLD);

        tabPersonalInfo.setBackgroundResource(android.R.color.transparent);
        tabPersonalInfo.setTextColor(getColor(R.color.text_secondary));
        tabPersonalInfo.setTypeface(null, android.graphics.Typeface.NORMAL);

        cardPersonalInfo.setVisibility(View.GONE);
        layoutBookingHistory.setVisibility(View.VISIBLE);
    }

    private void bindProfile(User user) {
        currentUser = user;
        String name = user.getFullName() == null ? "" : user.getFullName();
        tvName.setText(name);
        tvEmail.setText(user.getEmail() == null ? "" : user.getEmail());
        tvAvatar.setText(getInitials(name));
        setText(tilName, name);
        setText(tilPhone, user.getPhone() == null ? "" : user.getPhone());
        animateTrustScore(user.getTrustScore());
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = getTextValue(tilName);
        String phone = getTextValue(tilPhone);
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_empty_fields));
            return;
        }
        profileViewModel.updateProfile(currentUser.getId(), name, phone);
    }

    private void setText(TextInputLayout layout, String value) {
        if (layout.getEditText() != null) {
            layout.getEditText().setText(value);
        }
    }

    private String getTextValue(TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) {
            return "";
        }
        return layout.getEditText().getText().toString().trim();
    }

    private void animateTrustScore(int score) {
        int safeScore = Math.max(0, Math.min(100, score));
        tvTrustScore.setText(getString(R.string.trust_score_value, safeScore));
        android.animation.ObjectAnimator animation =
                android.animation.ObjectAnimator.ofInt(pbTrustScore, "progress", pbTrustScore.getProgress(), safeScore);
        animation.setDuration(600);
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animation.start();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.US);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(Locale.US);
    }

    @Override
    public void onCancel(long bookingId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancel_booking_title)
                .setMessage(R.string.cancel_booking_message)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_confirm_cancel, (dialog, which) -> {
                    pendingCancelBookingId = bookingId;
                    bookingViewModel.cancelBooking(bookingId);
                })
                .show();
    }

    @Override
    public void onQrCheckin(Booking booking) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.qr_dialog_title)
                .setMessage(getString(R.string.qr_dialog_message, booking.getId()))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onDirections(Booking booking) {
        String query = booking.getFieldName() != null ? booking.getFieldName() : "";
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=" + Uri.encode(query)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
    }
}
