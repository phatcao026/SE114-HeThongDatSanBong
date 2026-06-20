package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldFilter;
import com.example.timsanbong.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class FindPitchFragment extends Fragment {

    private FieldViewModel fieldViewModel;
    private FieldAdapter fieldAdapter;
    private RecyclerView rvFields;
    private EditText etSearchFields;
    private TextView tvResultCount;
    private TextView tvFilterState;
    private TextView tvEmptyState;
    private TextView tvErrorState;
    private View pbLoading;
    private TextView chipTypeAll, chipType5, chipType7;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_customer_find_pitch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupList();
        setupSearchAndFilter(view);
        setupObservers();

        fieldViewModel.loadFields();
    }

    private void initViews(View view) {
        rvFields = view.findViewById(R.id.rvFields);
        etSearchFields = view.findViewById(R.id.etSearchFields);
        tvResultCount = view.findViewById(R.id.tvResultCount);
        tvFilterState = view.findViewById(R.id.tvFilterState);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvErrorState = view.findViewById(R.id.tvErrorState);
        pbLoading = view.findViewById(R.id.pbLoading);
        chipTypeAll = view.findViewById(R.id.chipTypeAll);
        chipType5 = view.findViewById(R.id.chipType5);
        chipType7 = view.findViewById(R.id.chipType7);

        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);
    }

    private void setupList() {
        fieldAdapter = new FieldAdapter(new ArrayList<>(), this::openFieldDetail);
        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFields.setAdapter(fieldAdapter);
    }

    private void setupSearchAndFilter(View view) {
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

        ImageButton btnFilter = view.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment sheet =
                    FilterBottomSheetFragment.newInstance(fieldViewModel.getCurrentFilter());
            sheet.setOnFilterApplyListener(filter -> {
                updateFilterLabel(filter);
                updateTypeChips(filter == null ? null : filter.fieldType);
            });
            sheet.show(getChildFragmentManager(), "field_filters");
        });

        chipTypeAll.setOnClickListener(v -> selectTypeChip(null));
        chipType5.setOnClickListener(v -> selectTypeChip("5 người"));
        chipType7.setOnClickListener(v -> selectTypeChip("7 người"));
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
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected
                ? R.drawable.bg_chip_filter_selected
                : R.drawable.bg_chip_filter);
        chip.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.primary_dark : R.color.text_secondary));
    }

    private void setupObservers() {
        fieldViewModel.fieldsState.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading();
            } else if (resource.status == Resource.Status.ERROR) {
                showError(resource.message);
            }
        });

        fieldViewModel.filteredFields.observe(getViewLifecycleOwner(), fields -> {
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
        Intent intent = new Intent(requireContext(), FieldDetailActivity.class);
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
