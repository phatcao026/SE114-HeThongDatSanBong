package com.example.timsanbong.ui.customer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.data.model.PaymentResponse;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {
    static final String PREF_PAYMENT_CHECKOUT = "payment_checkout";
    static final String KEY_PENDING_BOOKING_ID = "pending_booking_id";
    static final String KEY_PENDING_FIELD_NAME = "pending_field_name";
    static final String KEY_PENDING_TOTAL_AMOUNT = "pending_total_amount";
    static final String KEY_PENDING_DUE_AMOUNT = "pending_due_amount";

    private TextView tvFieldName;
    private TextView tvBookingRef;
    private TextView tvTotalPrice;
    private TextView tvDepositAmount;
    private TextView tvRemainderAmount;
    private TextView tvStateMessage;
    private MaterialButton btnPay;
    private MaterialButton btnViewBookings;
    private android.widget.ProgressBar pbLoading;
    private android.widget.TextView tvEmptyState;
    private android.widget.TextView tvErrorState;
    private PaymentViewModel paymentViewModel;
    private long bookingId;
    private String fieldName;
    private double totalPrice;
    private double amountDue;
    private String checkoutUrl;
    private String checkoutSessionId;
    private boolean checkoutLaunched;
    private boolean syncingAfterReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment);

        tvFieldName = findViewById(R.id.tvFieldName);
        tvBookingRef = findViewById(R.id.tvBookingRef);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvDepositAmount = findViewById(R.id.tvDepositAmount);
        tvRemainderAmount = findViewById(R.id.tvRemainderAmount);
        tvStateMessage = findViewById(R.id.tvStateMessage);
        btnPay = findViewById(R.id.btnPay);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);

        bookingId = getIntent().getLongExtra(Constants.EXTRA_BOOKING_ID, -1);
        fieldName = getIntent().getStringExtra(Constants.EXTRA_PAYMENT_FIELD_NAME);
        totalPrice = getIntent().getDoubleExtra(Constants.EXTRA_TOTAL_PRICE, 0);
        amountDue = getIntent().getDoubleExtra(Constants.EXTRA_DEPOSIT_AMOUNT, totalPrice);
        if (amountDue <= 0) {
            amountDue = totalPrice;
        }
        
        if (bookingId == -1 || fieldName == null || amountDue <= 0) {
            showEmptyState();
            btnPay.setEnabled(false);
            return;
        }

        tvBookingRef.setText("Mã đơn: #BK-" + bookingId);
        tvFieldName.setText(fieldName);
        tvTotalPrice.setText(String.format("%,.0f %s", totalPrice, getString(R.string.currency_vnd)));
        tvDepositAmount.setText(String.format("%,.0f %s", amountDue, getString(R.string.currency_vnd)));
        
        double remainder = totalPrice - amountDue;
        tvRemainderAmount.setText(String.format("%,.0f %s", remainder > 0 ? remainder : 0, getString(R.string.currency_vnd)));

        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        paymentViewModel.paymentState.observe(this, state -> {
            if (state == null) {
                return;
            }
            if (state.status == PaymentViewModel.Status.INIT) {
                showContentState();
                tvStateMessage.setText("");
                btnPay.setText(R.string.action_pay_now);
                btnPay.setEnabled(true);
            } else if (state.status == PaymentViewModel.Status.PROCESSING) {
                showLoadingState();
                tvStateMessage.setText(getString(R.string.payment_processing));
                btnPay.setEnabled(false);
            } else if (state.status == PaymentViewModel.Status.CHECKOUT_READY) {
                checkoutUrl = state.checkoutUrl;
                checkoutSessionId = state.checkoutSessionId;
                showPendingCheckoutState();
                openCheckout(checkoutUrl);
            } else if (state.status == PaymentViewModel.Status.VERIFYING) {
                showLoadingState();
                tvStateMessage.setText(getString(R.string.payment_verifying));
            } else if (state.status == PaymentViewModel.Status.PAID) {
                syncingAfterReturn = false;
                openPaymentResult(true, state.payment, getString(R.string.payment_success_message));
            } else if (state.status == PaymentViewModel.Status.CANCELLED) {
                syncingAfterReturn = false;
                openPaymentResult(false, state.payment, getString(R.string.payment_failed_message));
            } else if (state.status == PaymentViewModel.Status.PENDING) {
                syncingAfterReturn = false;
                showPendingCheckoutState();
                tvStateMessage.setText(R.string.payment_pending_message);
            } else if (state.status == PaymentViewModel.Status.FAILED) {
                syncingAfterReturn = false;
                showErrorState(state.message);
                btnPay.setEnabled(true);
            }
        });

        btnPay.setOnClickListener(v -> {
            if (checkoutUrl != null && !checkoutUrl.trim().isEmpty()) {
                openCheckout(checkoutUrl);
                return;
            }
            PaymentRequest request = new PaymentRequest(bookingId, fieldName, amountDue);
            paymentViewModel.startPayment(request);
        });

        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkoutLaunched && !syncingAfterReturn
                && bookingId > 0) {
            syncingAfterReturn = true;
            paymentViewModel.refreshBookingPayment(bookingId);
        }
    }

    private void openCheckout(String checkoutUrl) {
        if (checkoutUrl == null || checkoutUrl.trim().isEmpty()) {
            return;
        }
        rememberPendingCheckout();
        checkoutLaunched = true;
        Uri uri = Uri.parse(checkoutUrl);
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.launchUrl(this, uri);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    private void openPaymentResult(boolean success, PaymentResponse payment, String fallbackMessage) {
        Intent intent = new Intent(this, PaymentResultActivity.class);
        long resultBookingId = payment != null && payment.getBookingId() != null
                ? payment.getBookingId()
                : bookingId;
        double amount = payment != null && payment.getAmountValue() > 0
                ? payment.getAmountValue()
                : amountDue;
        double remainder = success ? Math.max(0, totalPrice - amount) : -1;
        intent.putExtra(Constants.EXTRA_PAYMENT_SUCCESS, success);
        intent.putExtra(Constants.EXTRA_PAYMENT_MESSAGE,
                payment != null && payment.getMessage() != null ? payment.getMessage() : fallbackMessage);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, resultBookingId);
        intent.putExtra(Constants.EXTRA_TOTAL_PRICE, totalPrice);
        intent.putExtra(Constants.EXTRA_DEPOSIT_AMOUNT, success ? amount : -1);
        intent.putExtra(Constants.EXTRA_REMAINDER_AMOUNT, remainder);
        startActivity(intent);
        finish();
    }

    private void rememberPendingCheckout() {
        SharedPreferences prefs = getSharedPreferences(PREF_PAYMENT_CHECKOUT, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_PENDING_BOOKING_ID, bookingId)
                .putString(KEY_PENDING_FIELD_NAME, fieldName)
                .putFloat(KEY_PENDING_TOTAL_AMOUNT, (float) totalPrice)
                .putFloat(KEY_PENDING_DUE_AMOUNT, (float) amountDue)
                .apply();
    }

    private void showLoadingState() {
        pbLoading.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
        btnViewBookings.setVisibility(android.view.View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showPendingCheckoutState() {
        showContentState();
        tvStateMessage.setText(R.string.payment_pending_message);
        btnPay.setText(R.string.action_reopen_checkout);
        btnPay.setEnabled(true);
        btnViewBookings.setVisibility(android.view.View.VISIBLE);
    }

    private void showEmptyState() {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setVisibility(android.view.View.GONE);
        btnViewBookings.setVisibility(android.view.View.GONE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setText(message == null || message.trim().isEmpty()
                ? getString(R.string.payment_failed_message)
                : message);
        btnViewBookings.setVisibility(android.view.View.GONE);
        Toast.makeText(this, tvErrorState.getText(), Toast.LENGTH_SHORT).show();
    }
}
