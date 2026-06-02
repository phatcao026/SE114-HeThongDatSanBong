package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;

public class PaymentResultActivity extends AppCompatActivity {

    private TextView tvResultTitle;
    private TextView tvResultMessage;
    private MaterialButton btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        btnBackHome = findViewById(R.id.btnBackHome);

        boolean success = getIntent().getBooleanExtra(Constants.EXTRA_PAYMENT_SUCCESS, false);
        String message = getIntent().getStringExtra(Constants.EXTRA_PAYMENT_MESSAGE);

        if (success) {
            tvResultTitle.setText(R.string.payment_success_title);
            tvResultMessage.setText(message != null ? message : getString(R.string.payment_success_message));
        } else {
            tvResultTitle.setText(R.string.payment_failed_title);
            tvResultMessage.setText(message != null ? message : getString(R.string.payment_failed_message));
        }

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}

