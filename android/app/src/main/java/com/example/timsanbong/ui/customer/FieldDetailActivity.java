package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;

public class FieldDetailActivity extends AppCompatActivity {

    private ImageView ivFieldImage;
    private TextView tvFieldName, tvAddress, tvPrice, tvDescription, tvFieldType;
    private MaterialButton btnBook, btnMyBookings;
    private FieldViewModel fieldViewModel;
    private long fieldId;
    private androidx.core.widget.NestedScrollView scrollContent;
    private android.widget.ProgressBar pbLoading;
    private TextView tvEmptyState;
    private TextView tvErrorState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_detail);

        fieldId = getIntent().getLongExtra("fieldId", -1);

        ivFieldImage = findViewById(R.id.ivFieldImage);
        tvFieldName = findViewById(R.id.tvFieldName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvFieldType = findViewById(R.id.tvFieldType);
        btnBook = findViewById(R.id.btnBook);
        btnMyBookings = findViewById(R.id.btnMyBookings);
        scrollContent = findViewById(R.id.scrollContent);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);

        btnMyBookings.setOnClickListener(v -> startActivity(new Intent(this, MyBookingsActivity.class)));

        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);

        fieldViewModel.fieldDetailState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoadingState();
            } else if (resource.status == Resource.Status.SUCCESS) {
                if (resource.data == null) {
                    showEmptyState();
                } else {
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
            showEmptyState();
        }
    }

    private void bindField(Field field) {
        tvFieldName.setText(field.getName());
        tvAddress.setText(field.getAddress());
        tvDescription.setText(field.getDescription());
        tvFieldType.setText(field.getFieldType());

        String priceText = String.format("%,.0f %s", field.getPricePerHour(), getString(R.string.price_per_hour));
        tvPrice.setText(priceText);

        String imageUrl = field.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            ivFieldImage.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(ivFieldImage);
        }

        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("fieldId", field.getId());
            intent.putExtra("fieldName", field.getName());
            intent.putExtra("pricePerHour", field.getPricePerHour());
            startActivity(intent);
        });
    }

    private void showLoadingState() {
        pbLoading.setVisibility(android.view.View.VISIBLE);
        scrollContent.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(android.view.View.GONE);
        scrollContent.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showEmptyState() {
        pbLoading.setVisibility(android.view.View.GONE);
        scrollContent.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setVisibility(android.view.View.GONE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(android.view.View.GONE);
        scrollContent.setVisibility(android.view.View.GONE);
        tvEmptyState.setVisibility(android.view.View.GONE);
        tvErrorState.setVisibility(android.view.View.VISIBLE);
        tvErrorState.setText(message);
    }

    private String getSafeMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return getString(R.string.error_unknown);
        }
        return message;
    }
}
