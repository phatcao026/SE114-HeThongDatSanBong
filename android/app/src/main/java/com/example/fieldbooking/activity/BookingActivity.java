package com.example.fieldbooking.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fieldbooking.R;
import com.example.fieldbooking.model.Booking;
import com.example.fieldbooking.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private TextView tvFieldName, tvPrice;
    private TextInputEditText etDate, etStartTime, etEndTime;
    private MaterialButton btnConfirm;
    private long fieldId;
    private double pricePerHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        fieldId = getIntent().getLongExtra("fieldId", -1);
        pricePerHour = getIntent().getDoubleExtra("pricePerHour", 0);
        String fieldName = getIntent().getStringExtra("fieldName");

        tvFieldName = findViewById(R.id.tvFieldName);
        tvPrice = findViewById(R.id.tvPrice);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvFieldName.setText(fieldName);
        tvPrice.setText(String.format("%,.0f %s", pricePerHour, getString(R.string.price_per_hour)));

        etDate.setOnClickListener(v -> showDatePicker());

        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, day);
            etDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void confirmBooking() {
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String startTime = etStartTime.getText() != null ? etStartTime.getText().toString().trim() : "";
        String endTime = etEndTime.getText() != null ? etEndTime.getText().toString().trim() : "";

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("fieldId", fieldId);
        body.put("bookingDate", date);
        body.put("startTime", startTime);
        body.put("endTime", endTime);

        ApiClient.getService(this).createBooking(body).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                btnConfirm.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(BookingActivity.this, R.string.booking_success, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(BookingActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                btnConfirm.setEnabled(true);
                Toast.makeText(BookingActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
