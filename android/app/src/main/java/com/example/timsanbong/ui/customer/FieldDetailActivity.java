package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FieldDetailActivity extends AppCompatActivity {

    private ImageView ivFieldImage, btnBack;
    private TextView tvFieldName, tvAddress, tvPrice, tvDescription, tvRating;
    private MaterialButton btnBook;
    private FieldViewModel fieldViewModel;
    private long fieldId;
    private androidx.core.widget.NestedScrollView scrollContent;
    private android.widget.ProgressBar pbLoading;
    private TextView tvErrorState;
    
    // New UI Elements
    private RecyclerView rvDateChips;
    private RecyclerView rvTimeSlots;
    private TextView tvSelectedSlot;
    private TextView tvSelectedSlotLabel;
    private TextView tvNoSlots;
    private android.widget.ProgressBar pbSlots;

    private DateChipAdapter dateChipAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    // Selections
    private String selectedDate = "";
    private String selectedTimeRange = "";
    private long selectedSlotId = -1;
    private double selectedPrice = 0;
    private Field currentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_field_detail);

        fieldId = getIntent().getLongExtra("fieldId",
                getIntent().getLongExtra("FIELD_ID", -1));

        ivFieldImage = findViewById(R.id.ivFieldImage);
        btnBack = findViewById(R.id.btnBack);
        tvFieldName = findViewById(R.id.tvFieldName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvRating = findViewById(R.id.tvRating);
        btnBook = findViewById(R.id.btnBook);
        scrollContent = findViewById(R.id.scrollContent);
        pbLoading = findViewById(R.id.pbLoading);
        tvErrorState = findViewById(R.id.tvErrorState);
        
        rvDateChips = findViewById(R.id.rvDateChips);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);
        tvSelectedSlotLabel = findViewById(R.id.tvSelectedSlotLabel);
        tvNoSlots = findViewById(R.id.tvNoSlots);
        pbSlots = findViewById(R.id.pbSlots);

        btnBack.setOnClickListener(v -> finish());
        btnBook.setEnabled(false);

        setupDateChips();
        setupTimeSlots();
        updateStickyBar();

        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);

        fieldViewModel.fieldDetailState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoadingState();
            } else if (resource.status == Resource.Status.SUCCESS) {
                if (resource.data == null) {
                    showErrorState("Không tìm thấy thông tin sân");
                } else {
                    currentField = resource.data;
                    showContentState();
                    bindField(resource.data);
                }
            } else {
                String message = getSafeMessage(resource.message);
                showErrorState(message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        if (fieldId != -1) {
            fieldViewModel.loadFieldDetail(fieldId);
        } else {
            showErrorState("Không tìm thấy thông tin sân");
        }
        
        fieldViewModel.timeSlotsState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                pbSlots.setVisibility(View.VISIBLE);
                rvTimeSlots.setVisibility(View.GONE);
                tvNoSlots.setVisibility(View.GONE);
            } else if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                pbSlots.setVisibility(View.GONE);
                List<TimeSlotAdapter.TimeSlot> slots = new ArrayList<>();
                for (com.example.timsanbong.data.model.TimeSlotResponse slotResp : resource.data) {
                    double price = slotResp.getPrice() != null ? slotResp.getPrice() : 0;
                    slots.add(new TimeSlotAdapter.TimeSlot(slotResp.getId(),
                            formatTime(slotResp.getStartTime()), formatTime(slotResp.getEndTime()),
                            slotResp.isAvailable(), price));
                }
                timeSlotAdapter.updateSlots(slots);
                rvTimeSlots.setVisibility(slots.isEmpty() ? View.GONE : View.VISIBLE);
                tvNoSlots.setVisibility(slots.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (resource.status == Resource.Status.ERROR) {
                pbSlots.setVisibility(View.GONE);
                rvTimeSlots.setVisibility(View.GONE);
                tvNoSlots.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Không thể tải giờ trống", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupDateChips() {
        List<DateChipAdapter.DateChip> dates = new ArrayList<>();
        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sunday
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            String fullDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, dayOfMonth);
            
            dates.add(new DateChipAdapter.DateChip(days[dayOfWeek], String.valueOf(dayOfMonth), fullDate));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Select first by default
        selectedDate = dates.get(0).fullDate;
        
        dateChipAdapter = new DateChipAdapter(dates, dateChip -> {
            selectedDate = dateChip.fullDate;
            loadTimeSlotsForDate(selectedDate);
            // Reset time slot selection
            selectedTimeRange = "";
            selectedSlotId = -1;
            updateStickyBar();
        });
        
        rvDateChips.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDateChips.setAdapter(dateChipAdapter);
    }
    
    private void setupTimeSlots() {
        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 2));
        timeSlotAdapter = new TimeSlotAdapter(new ArrayList<>(), slot -> {
            selectedTimeRange = slot.timeRange();
            selectedSlotId = slot.id;
            selectedPrice = slot.price;
            updateStickyBar();
        });
        rvTimeSlots.setAdapter(timeSlotAdapter);
        // Load initial empty slots until field is loaded
    }
    
    private void loadTimeSlotsForDate(String date) {
        if (currentField == null) return;
        fieldViewModel.loadTimeSlots(currentField.getId(), date);
    }

    private void updateStickyBar() {
        if (selectedTimeRange.isEmpty()) {
            tvSelectedSlotLabel.setText("Khung giờ đã chọn");
            tvSelectedSlot.setText("Chưa chọn");
            btnBook.setText(R.string.action_select_slot);
            btnBook.setEnabled(false);
        } else {
            tvSelectedSlotLabel.setText(selectedTimeRange);
            tvSelectedSlot.setText(String.format("%,.0f đ", selectedPrice));
            btnBook.setText(R.string.action_hold_slot);
            btnBook.setEnabled(true);
        }
    }

    /** "18:00:00" → "18:00" */
    private String formatTime(String time) {
        if (time == null) return "";
        return time.length() >= 5 ? time.substring(0, 5) : time;
    }

    private void bindField(Field field) {
        tvFieldName.setText(field.getName());
        tvAddress.setText(field.getAddress());
        tvDescription.setText(field.getDescription());
        tvRating.setText(String.format("%.1f", field.getAverageRating() != null ? field.getAverageRating() : 0));

        double minPrice = field.getPricePerHour();
        if (minPrice > 0) {
            tvPrice.setText(String.format("Từ %,.0f %s", minPrice, getString(R.string.price_per_hour)));
        } else {
            tvPrice.setText(R.string.price_by_slot);
        }

        String imageUrl = field.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            ivFieldImage.setImageResource(R.drawable.bg_pitch_cover_square);
        } else {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_pitch_cover_square)
                    .error(R.drawable.bg_pitch_cover_square)
                    .centerCrop()
                    .into(ivFieldImage);
        }

        // Load initial time slots now that we have the field info (price etc)
        loadTimeSlotsForDate(selectedDate);

        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("fieldId", field.getId());
            intent.putExtra("fieldName", field.getName());
            intent.putExtra("timeSlotId", selectedSlotId);
            intent.putExtra("bookingDate", selectedDate);
            intent.putExtra("bookingTime", selectedTimeRange);
            intent.putExtra("totalPrice", selectedPrice);
            startActivity(intent);
        });
    }

    private void showLoadingState() {
        pbLoading.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.GONE);
        findViewById(R.id.layoutBookBar).setVisibility(View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        tvErrorState.setVisibility(View.GONE);
        findViewById(R.id.layoutBookBar).setVisibility(View.VISIBLE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.VISIBLE);
        tvErrorState.setText(message);
        findViewById(R.id.layoutBookBar).setVisibility(View.GONE);
    }

    private String getSafeMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return getString(R.string.error_unknown);
        }
        return message;
    }
}
