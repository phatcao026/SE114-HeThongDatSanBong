package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvFieldName;
    private TextView tvAmount;
    private TextView tvStateMessage;
    private MaterialButton btnPay;
    private android.widget.ProgressBar pbLoading;
    private android.widget.TextView tvEmptyState;
    private android.widget.TextView tvErrorState;
    private PaymentViewModel paymentViewModel;
    private long bookingId;
    private String fieldName;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment);

        tvFieldName = findViewById(R.id.tvFieldName);
        tvAmount = findViewById(R.id.tvAmount);
        tvStateMessage = findViewById(R.id.tvStateMessage);
        btnPay = findViewById(R.id.btnPay);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);

        bookingId = getIntent().getLongExtra(Constants.EXTRA_BOOKING_ID, -1);
        fieldName = getIntent().getStringExtra(Constants.EXTRA_PAYMENT_FIELD_NAME);
        totalPrice = getIntent().getDoubleExtra(Constants.EXTRA_TOTAL_PRICE, 0);

        if (bookingId == -1 || fieldName == null || fieldName.trim().isEmpty()) {
            showEmptyState();
            btnPay.setEnabled(false);
            return;
        }

        tvFieldName.setText(fieldName);
        tvAmount.setText(String.format("%,.0f %s", totalPrice, getString(R.string.currency_vnd)));

        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        paymentViewModel.paymentState.observe(this, state -> {
            if (state == null) {
                return;
            }
            if (state.status == PaymentViewModel.Status.INIT) {
                showContentState();
                tvStateMessage.setText("");
                btnPay.setEnabled(true);
            } else if (state.status == PaymentViewModel.Status.PROCESSING) {
                showLoadingState();
                tvStateMessage.setText(getString(R.string.payment_processing));
                btnPay.setEnabled(false);
            } else if (state.status == PaymentViewModel.Status.SUCCESS) {
                navigateToResult(true, getString(R.string.payment_success_field_format, fieldName));
            } else if (state.status == PaymentViewModel.Status.FAILED) {
                showErrorState(state.message);
                btnPay.setEnabled(true);
            }
        });

        btnPay.setOnClickListener(v -> {
            PaymentRequest request = new PaymentRequest(bookingId, fieldName, totalPrice);
            paymentViewModel.startPayment(request);
        });
    }

    private void navigateToResult(boolean success, String message) {
        Intent intent = new Intent(this, PaymentResultActivity.class);
        intent.putExtra(Constants.EXTRA_PAYMENT_SUCCESS, success);
        intent.putExtra(Constants.EXTRA_PAYMENT_MESSAGE, message);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, bookingId);
        intent.putExtra(Constants.EXTRA_DEPOSIT_AMOUNT,
                getIntent().getDoubleExtra(Constants.EXTRA_DEPOSIT_AMOUNT, -1));
        intent.putExtra(Constants.EXTRA_REMAINDER_AMOUNT,
                getIntent().getDoubleExtra(Constants.EXTRA_REMAINDER_AMOUNT, -1));
        startActivity(intent);
        finish();
    }

    private void showLoadingState() {
        pbLoading.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showEmptyState() {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setText(message);
    }
}

