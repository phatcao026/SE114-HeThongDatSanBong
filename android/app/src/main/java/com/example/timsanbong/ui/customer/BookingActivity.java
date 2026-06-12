package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BookingActivity extends AppCompatActivity {

    private TextView tvFieldName;
    private TextView tvBookingDate;
    private TextView tvBookingTime;
    private TextView tvTotalAmount;
    private TextView tvDepositAmount;
    private TextView tvPayNowAmount;
    private TextView tvBarAmount;
    private MaterialButton btnConfirm;
    private android.widget.ImageView btnBack;
    private FrameLayout layoutLoading;
    private MaterialCardView cardMethodMomo, cardMethodStripe, cardMethodBank;
    private RadioButton rbMomo, rbStripe, rbBank;
    private BookingViewModel bookingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_booking);

        tvFieldName = findViewById(R.id.tvFieldName);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvBookingTime = findViewById(R.id.tvBookingTime);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDepositAmount = findViewById(R.id.tvDepositAmount);
        tvPayNowAmount = findViewById(R.id.tvPayNowAmount);
        tvBarAmount = findViewById(R.id.tvBarAmount);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);
        layoutLoading = findViewById(R.id.layoutLoading);

        btnBack.setOnClickListener(v -> finish());

        setupPaymentMethods();

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // Read Intents
        long fieldId = getIntent().getLongExtra("fieldId", -1);
        long timeSlotId = getIntent().getLongExtra("timeSlotId", -1);
        String fieldName = getIntent().getStringExtra("fieldName");
        String bookingDate = getIntent().getStringExtra("bookingDate");
        String bookingTime = getIntent().getStringExtra("bookingTime");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0);
        double depositAmount = totalPrice * 0.3; // 30% deposit (matches backend DEPOSIT_RATE)

        tvFieldName.setText(fieldName);

        // Format date from YYYY-MM-DD to DD/MM/YYYY
        if (bookingDate != null && bookingDate.contains("-")) {
            String[] parts = bookingDate.split("-");
            if (parts.length == 3) {
                tvBookingDate.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
            } else {
                tvBookingDate.setText(bookingDate);
            }
        } else {
            tvBookingDate.setText(bookingDate);
        }

        tvBookingTime.setText(bookingTime);

        String priceFormatted = String.format("%,.0f đ", totalPrice);
        String depositFormatted = String.format("%,.0f đ", depositAmount);

        tvTotalAmount.setText(priceFormatted);
        tvDepositAmount.setText(depositFormatted);
        tvPayNowAmount.setText(depositFormatted);
        tvBarAmount.setText(depositFormatted);

        btnConfirm.setOnClickListener(v ->
                bookingViewModel.createBooking(fieldId, timeSlotId, bookingDate));

        bookingViewModel.bookingCreateState.observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    btnConfirm.setEnabled(false);
                    layoutLoading.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    layoutLoading.setVisibility(View.GONE);
                    double deposit = resource.data.getDepositAmount() > 0
                            ? resource.data.getDepositAmount()
                            : depositAmount;
                    double total = resource.data.getTotalAmount() > 0
                            ? resource.data.getTotalAmount()
                            : totalPrice;
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra(Constants.EXTRA_BOOKING_ID, resource.data.getId());
                    intent.putExtra(Constants.EXTRA_PAYMENT_FIELD_NAME, fieldName);
                    intent.putExtra(Constants.EXTRA_TOTAL_PRICE, deposit);
                    intent.putExtra(Constants.EXTRA_DEPOSIT_AMOUNT, deposit);
                    intent.putExtra(Constants.EXTRA_REMAINDER_AMOUNT, total - deposit);
                    startActivity(intent);
                    finish();
                    break;
                case ERROR:
                    btnConfirm.setEnabled(true);
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupPaymentMethods() {
        cardMethodMomo = findViewById(R.id.cardMethodMomo);
        cardMethodStripe = findViewById(R.id.cardMethodStripe);
        cardMethodBank = findViewById(R.id.cardMethodBank);
        rbMomo = findViewById(R.id.rbMomo);
        rbStripe = findViewById(R.id.rbStripe);
        rbBank = findViewById(R.id.rbBank);

        cardMethodMomo.setOnClickListener(v -> selectMethod(0));
        cardMethodStripe.setOnClickListener(v -> selectMethod(1));
        cardMethodBank.setOnClickListener(v -> selectMethod(2));
    }

    private void selectMethod(int index) {
        int selected = getColor(R.color.primary);
        int normal = getColor(R.color.border_gray);
        cardMethodMomo.setStrokeColor(index == 0 ? selected : normal);
        cardMethodStripe.setStrokeColor(index == 1 ? selected : normal);
        cardMethodBank.setStrokeColor(index == 2 ? selected : normal);
        rbMomo.setChecked(index == 0);
        rbStripe.setChecked(index == 1);
        rbBank.setChecked(index == 2);
    }
}
