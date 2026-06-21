package com.example.timsanbong.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.User;
import com.example.timsanbong.ui.admin.AdminMainActivity;
import com.example.timsanbong.ui.auth.LoginActivity;
import com.example.timsanbong.ui.customer.BookingAdapter;
import com.example.timsanbong.ui.customer.BookingViewModel;
import com.example.timsanbong.ui.customer.CustomerMainActivity;
import com.example.timsanbong.ui.customer.FieldReviewDialog;
import com.example.timsanbong.ui.customer.PaymentActivity;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment implements BookingAdapter.OnBookingActionListener {

    private TextView tvAvatar, tvName, tvEmail, tvTrustScore;
    private android.widget.ProgressBar pbTrustScore;
    private TextView tabPersonalInfo, tabBookingHistory;
    private View cardPersonalInfo, layoutBookingHistory;
    private TextView tvEmptyBookings;
    private TextInputLayout tilName, tilPhone;
    private MaterialButton btnSaveProfile, btnLogout, btnAdminMode;
    private ProfileViewModel profileViewModel;
    private BookingViewModel bookingViewModel;
    private BookingAdapter bookingAdapter;
    private User currentUser;
    private long pendingCancelBookingId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupTabs();
        setupListeners();
        setupObservers();

        profileViewModel.loadProfile();
        bookingViewModel.loadMyBookings();
    }

    private void initViews(View view) {
        tvAvatar = view.findViewById(R.id.tvAvatar);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTrustScore = view.findViewById(R.id.tvTrustScore);
        pbTrustScore = view.findViewById(R.id.pbTrustScore);
        tabPersonalInfo = view.findViewById(R.id.tabPersonalInfo);
        tabBookingHistory = view.findViewById(R.id.tabBookingHistory);
        cardPersonalInfo = view.findViewById(R.id.cardPersonalInfo);
        layoutBookingHistory = view.findViewById(R.id.layoutBookingHistory);
        tvEmptyBookings = view.findViewById(R.id.tvEmptyBookings);
        tilName = view.findViewById(R.id.tilName);
        tilPhone = view.findViewById(R.id.tilPhone);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnAdminMode = view.findViewById(R.id.btnAdminMode);

        View btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setVisibility(View.GONE);
        }

        SessionManager sessionManager = new SessionManager(requireContext());
        if (sessionManager.isAdmin()) {
            btnAdminMode.setVisibility(View.VISIBLE);
        }

        RecyclerView rvBookingHistory = view.findViewById(R.id.rvBookingHistory);
        rvBookingHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingAdapter = new BookingAdapter(new ArrayList<>(), this);
        rvBookingHistory.setAdapter(bookingAdapter);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> profileViewModel.logout());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnAdminMode.setOnClickListener(v -> startActivity(new Intent(requireContext(), AdminMainActivity.class)));
    }

    private void setupObservers() {
        profileViewModel.profileState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                bindProfile(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.profileUpdateState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            btnSaveProfile.setEnabled(resource.status != Resource.Status.LOADING);
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.logoutEvent.observe(getViewLifecycleOwner(), shouldLogout -> {
            if (Boolean.TRUE.equals(shouldLogout)) {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        bookingViewModel.bookingsState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.status != Resource.Status.SUCCESS) {
                return;
            }
            List<Booking> bookings = resource.data == null ? new ArrayList<>() : resource.data;
            bookingAdapter.updateBookings(bookings);
            tvEmptyBookings.setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
        });

        bookingViewModel.cancelState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS) {
                pendingCancelBookingId = -1;
                Toast.makeText(requireContext(), R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
                bookingViewModel.loadMyBookings();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        bookingViewModel.fieldReviewState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), R.string.field_review_success, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTabs() {
        tabPersonalInfo.setOnClickListener(v -> showPersonalInfo());
        tabBookingHistory.setOnClickListener(v -> showBookingHistory());
    }

    private void showPersonalInfo() {
        tabPersonalInfo.setBackgroundResource(R.drawable.bg_segment_active);
        tabPersonalInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        tabPersonalInfo.setTypeface(null, android.graphics.Typeface.BOLD);

        tabBookingHistory.setBackgroundResource(android.R.color.transparent);
        tabBookingHistory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        tabBookingHistory.setTypeface(null, android.graphics.Typeface.NORMAL);

        cardPersonalInfo.setVisibility(View.VISIBLE);
        layoutBookingHistory.setVisibility(View.GONE);
    }

    private void showBookingHistory() {
        tabBookingHistory.setBackgroundResource(R.drawable.bg_segment_active);
        tabBookingHistory.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        tabBookingHistory.setTypeface(null, android.graphics.Typeface.BOLD);

        tabPersonalInfo.setBackgroundResource(android.R.color.transparent);
        tabPersonalInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
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
            Toast.makeText(requireContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
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
        new MaterialAlertDialogBuilder(requireContext())
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
    public void onPay(Booking booking) {
        double dueAmount = booking.getDepositAmount() > 0
                ? booking.getDepositAmount()
                : booking.getTotalAmount();
        Intent intent = new Intent(requireContext(), PaymentActivity.class);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(Constants.EXTRA_PAYMENT_FIELD_NAME, booking.getFieldName());
        intent.putExtra(Constants.EXTRA_TOTAL_PRICE, booking.getTotalAmount());
        intent.putExtra(Constants.EXTRA_DEPOSIT_AMOUNT, dueAmount);
        intent.putExtra(Constants.EXTRA_REMAINDER_AMOUNT, booking.getTotalAmount() - dueAmount);
        startActivity(intent);
    }

    @Override
    public void onQrCheckin(Booking booking) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.qr_dialog_title)
                .setMessage(getString(R.string.qr_dialog_message, booking.getId()))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onRateField(Booking booking) {
        FieldReviewDialog.show(requireContext(), booking,
                (selectedBooking, rating, comment) ->
                        bookingViewModel.submitFieldReview(selectedBooking.getId(), rating, comment));
    }
}
