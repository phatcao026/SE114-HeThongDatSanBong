package com.example.timsanbong.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.adapter.FieldAdapter;
import com.example.timsanbong.model.Field;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────
    private RecyclerView rvFields;
    private TextInputEditText etSearch;
    private MaterialCardView btnFilter;
    private MaterialCardView cvAvatar;
    private TextView tvUserName;
    private TextView tvSectionFields;
    private NavBarManager navBarManager;

    // ── Data ─────────────────────────────────────────────
    private List<Field> allFields = new ArrayList<>();
    private String currentKeyword = "";
    private FilterState currentFilter = new FilterState();

    static class FilterState {
        String fieldType = null;   // null = all types
        int priceRange = 0;        // 0=all, 1=<200k, 2=200k–300k, 3=>300k
        boolean availableOnly = false;

        boolean isActive() {
            return fieldType != null || priceRange != 0 || availableOnly;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        rvFields       = findViewById(R.id.rvFields);
        etSearch       = findViewById(R.id.etSearch);
        btnFilter      = findViewById(R.id.btnFilter);
        cvAvatar       = findViewById(R.id.cvAvatar);
        tvUserName     = findViewById(R.id.tvUserName);
        tvSectionFields = findViewById(R.id.tvSectionFields);

        rvFields.setLayoutManager(new LinearLayoutManager(this));

        tvUserName.setText(loadUserDisplayName());

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_HOME);
        navBarManager.setup();
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim();
                applyFilters();
            }
        });

        btnFilter.setOnClickListener(v -> showFilterSheet());

        cvAvatar.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadData() {
        allFields = getMockFields();
        applyFilters();

        /* Uncomment to use real API endpoint
        ApiClient.getService(this).getFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFields = response.body();
                    applyFilters();
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void showFilterSheet() {
        FilterBottomSheetFragment sheet = FilterBottomSheetFragment.newInstance(currentFilter);
        sheet.setOnFilterApplyListener(newFilter -> {
            currentFilter = newFilter;
            updateFilterButtonState();
            applyFilters();
        });
        sheet.show(getSupportFragmentManager(), "filter");
    }

    private void applyFilters() {
        List<Field> result = new ArrayList<>(allFields);

        if (!currentKeyword.isEmpty()) {
            List<Field> searched = new ArrayList<>();
            for (Field f : result) {
                if (f.getName().toLowerCase().contains(currentKeyword.toLowerCase()) ||
                        f.getAddress().toLowerCase().contains(currentKeyword.toLowerCase())) {
                    searched.add(f);
                }
            }
            result = searched;
        }

        if (currentFilter.fieldType != null) {
            List<Field> typed = new ArrayList<>();
            for (Field f : result) {
                if (currentFilter.fieldType.equals(f.getFieldType())) typed.add(f);
            }
            result = typed;
        }

        if (currentFilter.priceRange != 0) {
            List<Field> priced = new ArrayList<>();
            for (Field f : result) {
                double p = f.getPricePerHour();
                boolean matches = false;
                if (currentFilter.priceRange == 1)      matches = p < 200_000;
                else if (currentFilter.priceRange == 2) matches = p >= 200_000 && p <= 300_000;
                else if (currentFilter.priceRange == 3) matches = p > 300_000;
                if (matches) priced.add(f);
            }
            result = priced;
        }

        if (currentFilter.availableOnly) {
            List<Field> available = new ArrayList<>();
            for (Field f : result) {
                if (f.isAvailable()) available.add(f);
            }
            result = available;
        }

        showFields(result);
    }

    private void updateFilterButtonState() {
        int color = currentFilter.isActive()
                ? getResources().getColor(R.color.primary_dark, getTheme())
                : getResources().getColor(R.color.primary, getTheme());
        btnFilter.setCardBackgroundColor(color);
    }

    private void showFields(List<Field> fields) {
        boolean isFiltered = !currentKeyword.isEmpty() || currentFilter.isActive();
        if (isFiltered) {
            tvSectionFields.setText(getString(R.string.section_results, fields.size()));
        } else {
            tvSectionFields.setText(R.string.section_featured_fields);
        }

        FieldAdapter adapter = new FieldAdapter(fields, field -> {
            Intent intent = new Intent(this, FieldDetailActivity.class);
            intent.putExtra("fieldId", field.getId());
            startActivity(intent);
        });
        rvFields.setAdapter(adapter);
    }

    private String loadUserDisplayName() {
        try {
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String userJson = prefs.getString("user_json", null);
            if (userJson != null) {
                JSONObject obj = new JSONObject(userJson);
                String fullName = obj.optString("fullName", "").trim();
                if (!fullName.isEmpty()) {
                    String[] parts = fullName.split("\\s+");
                    return parts[parts.length - 1];
                }
            }
        } catch (JSONException ignored) {}
        return getString(R.string.greeting_fallback);
    }

    private List<Field> getMockFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field(1, "Sân bóng Chảo Lửa",       "30 Phan Thúc Duyện, Tân Bình",  250000, "https://tbd.com", "Sân cỏ nhân tạo chất lượng tốt",         "5 người",  true));
        fields.add(new Field(2, "Sân bóng Đại học Y Dược", "Linh Trung, Thủ Đức",           200000, "https://tbd.com", "Sân 7 người, giá sinh viên",              "7 người",  true));
        fields.add(new Field(3, "Sân bóng Kỳ Hòa",         "Mai Lão Bạng, Tân Bình",        300000, "https://tbd.com", "10 sân liên kề, có đèn đêm",              "5 người",  true));
        fields.add(new Field(4, "Sân bóng Thăng Long",     "123 Quốc Lộ 13, Bình Thạnh",   220000, "https://tbd.com", "Sân trung tâm, tiện đi lại",              "5 người",  false));
        fields.add(new Field(5, "Sân bóng Phú Thọ",        "Nguyễn Thị Nhỏ, Tân Phú",      180000, "https://tbd.com", "Sân nhỏ, phù hợp tập luyện",              "5 người",  true));
        fields.add(new Field(6, "Sân bóng Bình Chánh",     "Quốc lộ 1A, Bình Chánh",       350000, "https://tbd.com", "Sân 11 người chuyên nghiệp, có khán đài", "11 người", true));
        return fields;
    }
}
