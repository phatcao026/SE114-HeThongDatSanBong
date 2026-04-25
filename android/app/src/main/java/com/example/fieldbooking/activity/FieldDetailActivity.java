package com.example.fieldbooking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fieldbooking.R;
import com.example.fieldbooking.model.Field;
import com.example.fieldbooking.network.ApiClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FieldDetailActivity extends AppCompatActivity {

    private ImageView ivFieldImage;
    private TextView tvFieldName, tvAddress, tvPrice, tvDescription, tvFieldType;
    private MaterialButton btnBook, btnMyBookings;
    private long fieldId;

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

        btnMyBookings.setOnClickListener(v -> startActivity(new Intent(this, MyBookingsActivity.class)));

        if (fieldId != -1) {
            loadFieldDetail(fieldId);
        }
    }

    private void loadFieldDetail(long id) {
        ApiClient.getService(this).getFieldById(id).enqueue(new Callback<Field>() {
            @Override
            public void onResponse(Call<Field> call, Response<Field> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindField(response.body());
                }
            }

            @Override
            public void onFailure(Call<Field> call, Throwable t) {
                Toast.makeText(FieldDetailActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindField(Field field) {
        tvFieldName.setText(field.getName());
        tvAddress.setText(field.getAddress());
        tvDescription.setText(field.getDescription());
        tvFieldType.setText(field.getFieldType());

        String priceText = String.format("%,.0f %s", field.getPricePerHour(), getString(R.string.price_per_hour));
        tvPrice.setText(priceText);

        Glide.with(this)
                .load(field.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivFieldImage);

        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("fieldId", field.getId());
            intent.putExtra("fieldName", field.getName());
            intent.putExtra("pricePerHour", field.getPricePerHour());
            startActivity(intent);
        });
    }
}
