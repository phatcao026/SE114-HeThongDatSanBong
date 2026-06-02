package com.example.timsanbong.ui.customer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    private TextView tvFieldName, tvPrice;
    private TextInputEditText etDate, etStartTime, etEndTime;
    private MaterialButton btnConfirm;
    private BookingViewModel bookingViewModel;
    private long fieldId;
    private double pricePerHour;
    private android.widget.ProgressBar pbLoading;
    private TextView tvEmptyState;
    private TextView tvErrorState;
    private TextView tvTitle;
    private com.google.android.material.card.MaterialCardView cardBooking;
    private TextView tvEstimatedTotal;
    private com.google.android.material.textfield.TextInputLayout tilDate;
    private com.google.android.material.textfield.TextInputLayout tilStartTime;
    private com.google.android.material.textfield.TextInputLayout tilEndTime;
    private String bookingDateApi;
    private Integer startMinutes;
    private Integer endMinutes;
    private boolean isSubmitting;
    private boolean hasAttemptedSubmit;
    private double lastEstimatedTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        fieldId = getIntent().getLongExtra("fieldId", -1);
        pricePerHour = getIntent().getDoubleExtra("pricePerHour", 0);
        String fieldName = getIntent().getStringExtra("fieldName");

        tvTitle = findViewById(R.id.tvTitle);
        cardBooking = findViewById(R.id.cardBooking);
        tvFieldName = findViewById(R.id.tvFieldName);
        tvPrice = findViewById(R.id.tvPrice);
        tvEstimatedTotal = findViewById(R.id.tvEstimatedTotal);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnConfirm = findViewById(R.id.btnConfirm);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);
        tilDate = findViewById(R.id.tilDate);
        tilStartTime = findViewById(R.id.tilStartTime);
        tilEndTime = findViewById(R.id.tilEndTime);

        if (fieldId == -1 || fieldName == null || fieldName.trim().isEmpty()) {
            showEmptyState();
            btnConfirm.setEnabled(false);
            return;
        }

        showContentState();
        tvFieldName.setText(fieldName);
        tvPrice.setText(String.format("%,.0f %s", pricePerHour, getString(R.string.price_per_hour)));
        tvEstimatedTotal.setText(getString(R.string.estimated_total, "--"));

        etDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
        btnConfirm.setOnClickListener(v -> confirmBooking());
        btnConfirm.setEnabled(false);

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        bookingViewModel.bookingCreateState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                isSubmitting = true;
                showLoadingState();
                btnConfirm.setEnabled(false);
            } else if (resource.status == Resource.Status.SUCCESS) {
                isSubmitting = false;
                showContentState();
                updateConfirmState();
                Toast.makeText(BookingActivity.this, R.string.booking_success, Toast.LENGTH_LONG).show();
                navigateToPayment();
                finish();
            } else {
                isSubmitting = false;
                String message = getSafeMessage(resource.message);
                showErrorState(message);
                updateConfirmState();
                Toast.makeText(BookingActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        addFormWatchers();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            bookingDateApi = String.format("%04d-%02d-%02d", year, month + 1, day);
            String displayDate = String.format("%02d/%02d/%04d", day, month + 1, year);
            etDate.setText(displayDate);
            updateEstimateAndValidation();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(getTodayStartMillis());
        dialog.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            int minutesValue = selectedHour * 60 + selectedMinute;
            String timeText = String.format("%02d:%02d", selectedHour, selectedMinute);
            if (isStart) {
                startMinutes = minutesValue;
                etStartTime.setText(timeText);
            } else {
                endMinutes = minutesValue;
                etEndTime.setText(timeText);
            }
            updateEstimateAndValidation();
        }, hour, minute, true).show();
    }

    private void confirmBooking() {
        if (isSubmitting) {
            return;
        }

        if (!isFormValid()) {
            hasAttemptedSubmit = true;
            updateInlineErrors(true);
            updateConfirmState();
            return;
        }

        isSubmitting = true;
        updateConfirmState();
        String startTime = etStartTime.getText() != null ? etStartTime.getText().toString().trim() : "";
        String endTime = etEndTime.getText() != null ? etEndTime.getText().toString().trim() : "";
        bookingViewModel.createBooking(fieldId, bookingDateApi, startTime, endTime);
    }

    private void addFormWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEstimateAndValidation();
            }
        };
        etDate.addTextChangedListener(watcher);
        etStartTime.addTextChangedListener(watcher);
        etEndTime.addTextChangedListener(watcher);
    }

    private void updateEstimateAndValidation() {
        updateEstimatedTotal();
        updateInlineErrors(false);
        updateConfirmState();
    }

    private void updateInlineErrors(boolean showErrors) {
        clearFieldErrors();
        if (!showErrors) {
            return;
        }
        if (bookingDateApi == null || bookingDateApi.trim().isEmpty()) {
            tilDate.setError(getString(R.string.error_empty_fields));
        } else if (!isSelectedDateValid()) {
            tilDate.setError(getString(R.string.error_invalid_date));
        }

        if (startMinutes == null) {
            tilStartTime.setError(getString(R.string.error_empty_fields));
        }
        if (endMinutes == null) {
            tilEndTime.setError(getString(R.string.error_empty_fields));
        } else if (startMinutes != null && endMinutes <= startMinutes) {
            tilEndTime.setError(getString(R.string.error_invalid_time_range));
        }
    }

    private void clearFieldErrors() {
        tilDate.setError(null);
        tilStartTime.setError(null);
        tilEndTime.setError(null);
    }

    private void updateConfirmState() {
        if (hasAttemptedSubmit) {
            updateInlineErrors(true);
        }
        btnConfirm.setEnabled(!isSubmitting && isFormValid());
    }

    private void updateEstimatedTotal() {
        if (startMinutes == null || endMinutes == null || endMinutes <= startMinutes) {
            lastEstimatedTotal = 0;
            tvEstimatedTotal.setText(getString(R.string.estimated_total, "--"));
            return;
        }
        double hours = (endMinutes - startMinutes) / 60.0;
        double total = pricePerHour * hours;
        lastEstimatedTotal = total;
        String totalText = String.format("%,.0f %s", total, getString(R.string.currency_vnd));
        tvEstimatedTotal.setText(getString(R.string.estimated_total, totalText));
    }

    private boolean isFormValid() {
        if (bookingDateApi == null || bookingDateApi.trim().isEmpty()) {
            return false;
        }
        if (!isSelectedDateValid()) {
            return false;
        }
        if (startMinutes == null || endMinutes == null) {
            return false;
        }
        return endMinutes > startMinutes;
    }

    private boolean isSelectedDateValid() {
        return getSelectedDateMillis() >= getTodayStartMillis();
    }

    private long getSelectedDateMillis() {
        if (bookingDateApi == null || bookingDateApi.trim().isEmpty()) {
            return 0;
        }
        String[] parts = bookingDateApi.split("-");
        if (parts.length != 3) {
            return 0;
        }
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getTodayStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void showLoadingState() {
        pbLoading.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
        tvTitle.setVisibility(android.view.View.GONE);
        cardBooking.setVisibility(android.view.View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
        tvTitle.setVisibility(android.view.View.VISIBLE);
        cardBooking.setVisibility(android.view.View.VISIBLE);
    }

    private void showEmptyState() {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setVisibility(android.view.View.GONE);
        tvTitle.setVisibility(android.view.View.GONE);
        cardBooking.setVisibility(android.view.View.GONE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setText(message);
        tvTitle.setVisibility(android.view.View.VISIBLE);
        cardBooking.setVisibility(android.view.View.VISIBLE);
    }

    private void navigateToPayment() {
        android.content.Intent intent = new android.content.Intent(this, PaymentActivity.class);
        intent.putExtra(com.example.timsanbong.utils.Constants.EXTRA_BOOKING_ID, System.currentTimeMillis());
        intent.putExtra(com.example.timsanbong.utils.Constants.EXTRA_PAYMENT_FIELD_NAME,
                tvFieldName.getText() != null ? tvFieldName.getText().toString() : "");
        intent.putExtra(com.example.timsanbong.utils.Constants.EXTRA_TOTAL_PRICE, lastEstimatedTotal);
        startActivity(intent);
    }

    private String getSafeMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return getString(R.string.error_unknown);
        }
        return message;
    }
}
