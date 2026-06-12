package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldFilter;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class FindPitchActivity extends AppCompatActivity {

    private FieldViewModel fieldViewModel;
    private FieldAdapter fieldAdapter;
    private RecyclerView rvFields;
    private EditText etSearchFields;
    private TextView tvResultCount;
    private TextView tvFilterState;
    private TextView tvEmptyState;
    private TextView tvErrorState;
    private View pbLoading;
    private TextView chipTypeAll, chipType5, chipType7, chipType11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_find_pitch);

        initViews();
        setupList();
        setupSearchAndFilter();
        setupObservers();

        new NavBarManager(this, NavBarManager.ITEM_SEARCH).setup();
        fieldViewModel.loadFields();
    }

    private void initViews() {
        rvFields = findViewById(R.id.rvFields);
        etSearchFields = findViewById(R.id.etSearchFields);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvFilterState = findViewById(R.id.tvFilterState);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvErrorState = findViewById(R.id.tvErrorState);
        pbLoading = findViewById(R.id.pbLoading);
        chipTypeAll = findViewById(R.id.chipTypeAll);
        chipType5 = findViewById(R.id.chipType5);
        chipType7 = findViewById(R.id.chipType7);
        chipType11 = findViewById(R.id.chipType11);

        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);
    }

    private void setupList() {
        fieldAdapter = new FieldAdapter(new ArrayList<>(), this::openFieldDetail);
        rvFields.setLayoutManager(new LinearLayoutManager(this));
        rvFields.setAdapter(fieldAdapter);
    }

    private void setupSearchAndFilter() {
        etSearchFields.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fieldViewModel.setSearchKeyword(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment sheet =
                    FilterBottomSheetFragment.newInstance(fieldViewModel.getCurrentFilter());
            sheet.setOnFilterApplyListener(filter -> {
                updateFilterLabel(filter);
                updateTypeChips(filter == null ? null : filter.fieldType);
            });
            sheet.show(getSupportFragmentManager(), "field_filters");
        });

        chipTypeAll.setOnClickListener(v -> selectTypeChip(null));
        chipType5.setOnClickListener(v -> selectTypeChip("5 người"));
        chipType7.setOnClickListener(v -> selectTypeChip("7 người"));
        chipType11.setOnClickListener(v -> selectTypeChip("11 người"));
    }

    private void selectTypeChip(String fieldType) {
        FieldFilter filter = fieldViewModel.getCurrentFilter();
        filter.fieldType = fieldType;
        fieldViewModel.setFilter(filter);
        updateTypeChips(fieldType);
    }

    private void updateTypeChips(String fieldType) {
        styleChip(chipTypeAll, fieldType == null);
        styleChip(chipType5, "5 người".equals(fieldType));
        styleChip(chipType7, "7 người".equals(fieldType));
        styleChip(chipType11, "11 người".equals(fieldType));
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected
                ? R.drawable.bg_chip_filter_selected
                : R.drawable.bg_chip_filter);
        chip.setTextColor(getColor(selected ? R.color.primary_dark : R.color.text_secondary));
    }

    private void setupObservers() {
        fieldViewModel.fieldsState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading();
            } else if (resource.status == Resource.Status.ERROR) {
                showError(resource.message);
            }
        });

        fieldViewModel.filteredFields.observe(this, fields -> {
            List<Field> safeFields = fields == null ? new ArrayList<>() : fields;
            fieldAdapter.updateFields(safeFields);
            tvResultCount.setText(getString(R.string.find_result_count, safeFields.size()));
            updateFilterLabel(fieldViewModel.getCurrentFilter());

            pbLoading.setVisibility(View.GONE);
            tvErrorState.setVisibility(View.GONE);
            rvFields.setVisibility(safeFields.isEmpty() ? View.GONE : View.VISIBLE);
            tvEmptyState.setVisibility(safeFields.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void updateFilterLabel(FieldFilter filter) {
        tvFilterState.setText(filter != null && filter.isActive()
                ? getString(R.string.filter_title)
                : getString(R.string.filter_all));
    }

    private void openFieldDetail(Field field) {
        Intent intent = new Intent(this, FieldDetailActivity.class);
        intent.putExtra("fieldId", field.getId());
        startActivity(intent);
    }

    private void showLoading() {
        pbLoading.setVisibility(View.VISIBLE);
        rvFields.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.GONE);
    }

    private void showError(String message) {
        pbLoading.setVisibility(View.GONE);
        rvFields.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        tvErrorState.setVisibility(View.VISIBLE);
        tvErrorState.setText(message == null || message.trim().isEmpty()
                ? getString(R.string.error_unknown)
                : message);
    }
}
