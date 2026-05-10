package com.example.timsanbong.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.adapter.FieldAdapter;
import com.example.timsanbong.model.Field;
import com.example.timsanbong.network.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFields;
    private FieldAdapter adapter;
    private TextInputEditText etSearch;
    private ImageView ivProfile;
    private NavBarManager navBarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        rvFields = findViewById(R.id.rvFields);
        etSearch = findViewById(R.id.etSearch);
        ivProfile = findViewById(R.id.ivProfile);

        rvFields.setLayoutManager(new LinearLayoutManager(this));

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_HOME);
        navBarManager.setup();
    }

    private void setupListeners() {
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadData();
                } else {
                    searchFields(keyword);
                }
            }
        });
    }

    private void loadData() {
        // Load mock data for preview and debugging
        List<Field> mockFields = getMockFields();
        showFields(mockFields);
        
        /* Uncomment to use real API endpoint
        ApiClient.getService(this).getFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showFields(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void searchFields(String keyword) {
        // Load mock filtered data for preview and debugging
        List<Field> mockFields = getMockFields();
        List<Field> filtered = new java.util.ArrayList<>();
        for (Field f : mockFields) {
            if (f.getName().toLowerCase().contains(keyword.toLowerCase()) || 
                f.getAddress().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(f);
            }
        }
        showFields(filtered);

        /* Uncomment to use real API endpoint
        ApiClient.getService(this).searchFields(keyword).enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showFields(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {}
        });
        */
    }

    private List<Field> getMockFields() {
        List<Field> fields = new java.util.ArrayList<>();
        fields.add(new Field(1, "Sân bóng Chảo Lửa", "30 Phan Thúc Duyện, Tân Bình", 250000, "https://tbd.com", "Sân cỏ nhân tạo chất lượng tốt", "5 người", true));
        fields.add(new Field(2, "Sân bóng Đại học Y Dược", "Linh Trung, Thủ Đức", 200000, "https://tbd.com", "Sân 7 người, giá sinh viên", "7 người", true));
        fields.add(new Field(3, "Sân bóng Kỳ Hòa", "Mai Lão Bạng, Tân Bình", 300000, "https://tbd.com", "10 sân liên kề, có đèn đêm", "5 người", true));
        fields.add(new Field(4, "Sân bóng Thăng Long", "123 Quốc Lộ 13, Bình Thạnh", 220000, "https://tbd.com", "Sân trung tâm, tiện đi lại", "5 người", false));
        return fields;
    }

    private void showFields(List<Field> fields) {
        adapter = new FieldAdapter(fields, field -> {
            Intent intent = new Intent(this, FieldDetailActivity.class);
            intent.putExtra("fieldId", field.getId());
            startActivity(intent);
        });
        rvFields.setAdapter(adapter);
    }
}
