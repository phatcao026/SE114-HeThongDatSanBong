package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.PaymentResponse;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PaymentResultActivity extends AppCompatActivity {

    private ImageView ivResultIcon;
    private TextView tvResultTitle;
    private TextView tvResultMessage;
    private TextView tvBookingId;
    private TextView tvStatus;
    private MaterialCardView cardBookingInfo;
    private MaterialButton btnPrimary;
    private MaterialButton btnSecondary;
    private PaymentViewModel paymentViewModel;
    private boolean refreshStarted;
    private boolean browserCancelled;
    private double expectedTotalAmount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment_result);

        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvStatus = findViewById(R.id.tvStatus);
        cardBookingInfo = findViewById(R.id.cardBookingInfo);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnSecondary = findViewById(R.id.btnSecondary);

        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        observePaymentState();
        setupActions();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        refreshStarted = false;
        handleIntent(intent);
    }

    private void observePaymentState() {
        paymentViewModel.paymentState.observe(this, state -> {
            if (state == null) {
                return;
            }
            if (state.status == PaymentViewModel.Status.VERIFYING) {
                showVerifyingState();
            } else if (state.status == PaymentViewModel.Status.PAID) {
                bindResult(true, state.payment, getString(R.string.payment_success_message));
            } else if (state.status == PaymentViewModel.Status.CANCELLED
                    || state.status == PaymentViewModel.Status.FAILED) {
                bindResult(false, state.payment, getString(R.string.payment_failed_message));
            } else if (state.status == PaymentViewModel.Status.PENDING) {
                if (browserCancelled) {
                    bindStaticResult(false, getString(R.string.payment_failed_message), -1, -1, -1);
                } else {
                    showPendingState();
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        browserCancelled = false;
        expectedTotalAmount = intent.getDoubleExtra(Constants.EXTRA_TOTAL_PRICE, -1);
        Uri data = intent.getData();
        if (data != null && "timsanbong".equals(data.getScheme())) {
            verifyDeepLink(data);
            return;
        }

        boolean success = intent.getBooleanExtra(Constants.EXTRA_PAYMENT_SUCCESS, false);
        String message = intent.getStringExtra(Constants.EXTRA_PAYMENT_MESSAGE);
        long bookingId = intent.getLongExtra(Constants.EXTRA_BOOKING_ID, -1);
        double paidAmount = intent.getDoubleExtra(Constants.EXTRA_DEPOSIT_AMOUNT, -1);
        double remainder = intent.getDoubleExtra(Constants.EXTRA_REMAINDER_AMOUNT, -1);

        bindStaticResult(success, message, bookingId, paidAmount, remainder);
    }

    private void verifyDeepLink(Uri data) {
        if (refreshStarted) {
            return;
        }
        refreshStarted = true;

        String result = data.getQueryParameter("result");
        String bookingIdParam = data.getQueryParameter("booking_id");
        String sessionId = data.getQueryParameter("session_id");
        browserCancelled = "cancel".equalsIgnoreCase(result)
                || "cancelled".equalsIgnoreCase(result)
                || "canceled".equalsIgnoreCase(result)
                || "failed".equalsIgnoreCase(result);

        SharedPreferences prefs = getSharedPreferences(
                PaymentActivity.PREF_PAYMENT_CHECKOUT,
                MODE_PRIVATE
        );

        long bookingId = parseLong(bookingIdParam, -1);
        if (bookingId <= 0) {
            bookingId = prefs.getLong(PaymentActivity.KEY_PENDING_BOOKING_ID, -1);
        }
        if (expectedTotalAmount <= 0) {
            expectedTotalAmount = prefs.getFloat(PaymentActivity.KEY_PENDING_TOTAL_AMOUNT, -1);
        }

        if (!browserCancelled && sessionId != null && !sessionId.trim().isEmpty()) {
            paymentViewModel.verifyCheckoutSession(sessionId);
            return;
        }

        if (bookingId > 0) {
            paymentViewModel.refreshBookingPayment(bookingId);
            return;
        }

        bindStaticResult(false, getString(R.string.payment_failed_message), -1, -1, -1);
    }

    private void bindResult(boolean success, PaymentResponse payment, String fallbackMessage) {
        long bookingId = payment != null && payment.getBookingId() != null ? payment.getBookingId() : -1;
        double amount = payment != null ? payment.getAmountValue() : -1;
        String message = payment != null && payment.getMessage() != null ? payment.getMessage() : fallbackMessage;
        double remainder = -1;
        if (success && expectedTotalAmount > 0 && amount > 0) {
            remainder = Math.max(0, expectedTotalAmount - amount);
        }
        bindStaticResult(success, message, bookingId, success ? amount : -1, remainder);
        if (success) {
            clearPendingCheckout();
        }
    }

    private void bindStaticResult(boolean success, String message, long bookingId,
                                  double paidAmount, double remainder) {
        if (success) {
            ivResultIcon.setImageResource(R.drawable.ic_check_circle);
            ivResultIcon.setColorFilter(getColor(R.color.primary));
            tvResultTitle.setText(R.string.payment_success_title);
            tvResultMessage.setText(message != null ? message : getString(R.string.payment_success_message));
            if (bookingId != -1) {
                tvBookingId.setText(String.format("#BK-%d", bookingId));
                tvStatus.setText(remainder > 0 ? R.string.status_deposit_paid : R.string.status_confirmed);
                cardBookingInfo.setVisibility(View.VISIBLE);
            } else {
                cardBookingInfo.setVisibility(View.GONE);
            }
            bindAmountRow(R.id.rowDeposit, R.id.tvDepositPaid, paidAmount);
            bindAmountRow(R.id.rowRemainder, R.id.tvRemainderAmount, remainder);
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_cancel_circle);
            ivResultIcon.setColorFilter(getColor(R.color.error));
            tvResultTitle.setText(R.string.payment_failed_title);
            tvResultMessage.setText(message != null ? message : getString(R.string.payment_failed_message));
            cardBookingInfo.setVisibility(View.GONE);
        }
    }

    private void showPendingState() {
        ivResultIcon.setImageResource(R.drawable.ic_credit_card);
        ivResultIcon.setColorFilter(getColor(R.color.primary));
        tvResultTitle.setText(R.string.payment_verifying_title);
        tvResultMessage.setText(R.string.payment_pending_message);
        cardBookingInfo.setVisibility(View.GONE);
    }

    private void showVerifyingState() {
        ivResultIcon.setImageResource(R.drawable.ic_credit_card);
        ivResultIcon.setColorFilter(getColor(R.color.primary));
        tvResultTitle.setText(R.string.payment_verifying_title);
        tvResultMessage.setText(R.string.payment_verifying);
        cardBookingInfo.setVisibility(View.GONE);
    }

    private void setupActions() {
        btnPrimary.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerMainActivity.class);
            intent.putExtra("SELECT_TAB", 0); // ITEM_HOME
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnSecondary.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerMainActivity.class);
            intent.putExtra("SELECT_TAB", 3); // ITEM_BOOKINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void bindAmountRow(int rowId, int textId, double amount) {
        View row = findViewById(rowId);
        if (amount > 0) {
            row.setVisibility(View.VISIBLE);
            ((TextView) findViewById(textId)).setText(String.format("%,.0f %s",
                    amount, getString(R.string.currency_vnd)));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private long parseLong(String value, long fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void clearPendingCheckout() {
        getSharedPreferences(PaymentActivity.PREF_PAYMENT_CHECKOUT, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
