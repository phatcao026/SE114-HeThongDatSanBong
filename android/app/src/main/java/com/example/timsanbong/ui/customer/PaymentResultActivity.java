package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PaymentResultActivity extends AppCompatActivity {

    private ImageView ivResultIcon;
    private TextView tvResultTitle;
    private TextView tvResultMessage;
    private TextView tvBookingId;
    private MaterialCardView cardBookingInfo;
    private MaterialButton btnPrimary;
    private MaterialButton btnSecondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment_result);

        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvBookingId = findViewById(R.id.tvBookingId);
        cardBookingInfo = findViewById(R.id.cardBookingInfo);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnSecondary = findViewById(R.id.btnSecondary);

        boolean success = getIntent().getBooleanExtra(Constants.EXTRA_PAYMENT_SUCCESS, false);
        String message = getIntent().getStringExtra(Constants.EXTRA_PAYMENT_MESSAGE);
        long bookingId = getIntent().getLongExtra(Constants.EXTRA_BOOKING_ID, -1);
        double deposit = getIntent().getDoubleExtra(Constants.EXTRA_DEPOSIT_AMOUNT, -1);
        double remainder = getIntent().getDoubleExtra(Constants.EXTRA_REMAINDER_AMOUNT, -1);

        if (success) {
            ivResultIcon.setImageResource(R.drawable.ic_check_circle);
            ivResultIcon.setColorFilter(getColor(R.color.primary));
            tvResultTitle.setText(R.string.payment_success_title);
            tvResultMessage.setText(message != null ? message : getString(R.string.payment_success_message));
            if (bookingId != -1) {
                tvBookingId.setText(String.format("#BK-%d", bookingId));
                cardBookingInfo.setVisibility(View.VISIBLE);
            } else {
                cardBookingInfo.setVisibility(View.GONE);
            }
            bindAmountRow(R.id.rowDeposit, R.id.tvDepositPaid, deposit);
            bindAmountRow(R.id.rowRemainder, R.id.tvRemainderAmount, remainder);
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_cancel_circle);
            ivResultIcon.setColorFilter(getColor(R.color.error));
            tvResultTitle.setText(R.string.payment_failed_title);
            tvResultMessage.setText(message != null ? message : getString(R.string.payment_failed_message));
            cardBookingInfo.setVisibility(View.GONE);
        }

        btnPrimary.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnSecondary.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyBookingsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void bindAmountRow(int rowId, int textId, double amount) {
        View row = findViewById(rowId);
        if (amount >= 0) {
            row.setVisibility(View.VISIBLE);
            ((TextView) findViewById(textId)).setText(String.format("%,.0f đ", amount));
        } else {
            row.setVisibility(View.GONE);
        }
    }
}
