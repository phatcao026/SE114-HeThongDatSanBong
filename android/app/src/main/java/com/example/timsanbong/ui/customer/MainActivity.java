package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.FieldFilter;
import com.example.timsanbong.ui.profile.ProfileActivity;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFields;
    private TextInputEditText etSearch;
    private MaterialCardView btnFilter;
    private MaterialCardView cvAvatar;
    private TextView tvUserName;
    private TextView tvSectionFields;
    private NavBarManager navBarManager;
    private FieldViewModel fieldViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupListeners();
        setupViewModel();
    }

    private void initViews() {
        rvFields = findViewById(R.id.rvFields);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
        cvAvatar = findViewById(R.id.cvAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvSectionFields = findViewById(R.id.tvSectionFields);

        rvFields.setLayoutManager(new LinearLayoutManager(this));

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_HOME);
        navBarManager.setup();
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (fieldViewModel != null) {
                    fieldViewModel.setSearchKeyword(s.toString());
                }
            }
        });

        btnFilter.setOnClickListener(v -> showFilterSheet());
        cvAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupViewModel() {
        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);

        fieldViewModel.userDisplayName.observe(this, name -> tvUserName.setText(name));

        fieldViewModel.fieldsState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                fieldViewModel.loadFields();
            } else if (resource.status == Resource.Status.ERROR) {
                fieldViewModel.loadFields();
            }
        });

        fieldViewModel.filteredFields.observe(this, fields -> {
            rvFields.setAdapter(new FieldAdapter(fields, field -> {
                Intent intent = new Intent(this, FieldDetailActivity.class);
                intent.putExtra("fieldId", field.getId());
                startActivity(intent);
            }));
            tvSectionFields.setText(String.format("Tổng cộng: %d sân", fields.size()));
        });

        fieldViewModel.loadFields();
        updateFilterButtonState();
    }

    private void showFilterSheet() {
        FilterBottomSheetFragment sheet = FilterBottomSheetFragment.newInstance(fieldViewModel.getCurrentFilter());
        sheet.setOnFilterApplyListener(filter -> {
            fieldViewModel.setFilter(filter);
            updateFilterButtonState();
        });
        sheet.show(getSupportFragmentManager(), "filter");
    }

    private void updateFilterButtonState() {
        btnFilter.setStrokeColor(fieldViewModel.isFilterActive() ?
                getColor(R.color.primary) : getColor(R.color.text_hint));
    }
}
