package com.example.timsanbong.ui.customer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private TextView tvFieldName, tvPrice;
    private TextInputEditText etDate, etStartTime, etEndTime;
    private MaterialButton btnConfirm;
    private BookingViewModel bookingViewModel;
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

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        bookingViewModel.bookingCreateState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                btnConfirm.setEnabled(false);
            } else if (resource.status == Resource.Status.SUCCESS) {
                btnConfirm.setEnabled(true);
                Toast.makeText(BookingActivity.this, R.string.booking_success, Toast.LENGTH_LONG).show();
                finish();
            } else {
                btnConfirm.setEnabled(true);
                Toast.makeText(BookingActivity.this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
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

        Map<String, Object> body = new HashMap<>();
        body.put("fieldId", fieldId);
        body.put("bookingDate", date);
        body.put("startTime", startTime);
        body.put("endTime", endTime);

        bookingViewModel.createBooking(body);
    }
}
