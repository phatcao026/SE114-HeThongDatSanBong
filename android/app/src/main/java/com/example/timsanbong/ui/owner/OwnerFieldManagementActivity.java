package com.example.timsanbong.ui.owner;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldCreateRequest;
import com.example.timsanbong.data.model.FieldUpdateRequest;
import com.example.timsanbong.data.model.TimeSlot;
import com.example.timsanbong.data.model.TimeSlotCreateRequest;
import com.example.timsanbong.data.model.TimeSlotUpdateRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class OwnerFieldManagementActivity extends AppCompatActivity {
    private OwnerFieldViewModel viewModel;
    private OwnerFieldAdapter fieldAdapter;
    private TextView tvEmptyFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_field_management);

        viewModel = new ViewModelProvider(this).get(OwnerFieldViewModel.class);
        tvEmptyFields = findViewById(R.id.tvOwnerEmptyFields);

        setupRecyclerView();
        setupActions();
        setupObservers();
        new OwnerNavBarManager(this, OwnerNavBarManager.ITEM_FIELDS).setup();

        viewModel.loadFields();
    }

    private void setupRecyclerView() {
        RecyclerView rvFields = findViewById(R.id.rvOwnerFields);
        fieldAdapter = new OwnerFieldAdapter(new OwnerFieldAdapter.Listener() {
            @Override
            public void onEditField(Field field) {
                showFieldForm(field);
            }

            @Override
            public void onDeleteField(Field field) {
                confirmDeleteField(field);
            }

            @Override
            public void onManageTimeSlots(Field field) {
                showTimeSlotManager(field);
            }
        });
        rvFields.setLayoutManager(new LinearLayoutManager(this));
        rvFields.setAdapter(fieldAdapter);
    }

    private void setupActions() {
        MaterialButton btnAddField = findViewById(R.id.btnAddOwnerField);
        btnAddField.setOnClickListener(v -> showFieldForm(null));
    }

    private void setupObservers() {
        viewModel.getFields().observe(this, fields -> {
            fieldAdapter.submitList(fields);
            tvEmptyFields.setVisibility(fields == null || fields.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFieldForm(Field field) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_owner_field_form, null);
        TextInputEditText etName = view.findViewById(R.id.etOwnerFieldName);
        TextInputEditText etType = view.findViewById(R.id.etOwnerFieldType);
        TextInputEditText etAddress = view.findViewById(R.id.etOwnerFieldAddress);
        TextInputEditText etDescription = view.findViewById(R.id.etOwnerFieldDescription);
        TextInputEditText etCoverImage = view.findViewById(R.id.etOwnerFieldCoverImage);
        TextInputEditText etStatus = view.findViewById(R.id.etOwnerFieldStatus);

        boolean isEditing = field != null;
        if (isEditing) {
            etName.setText(field.getName());
            etType.setText(field.getFieldType());
            etAddress.setText(field.getAddress());
            etDescription.setText(field.getDescription());
            etCoverImage.setText(field.getImageUrl());
            etStatus.setText(field.isAvailable() ? "AVAILABLE" : "MAINTENANCE");
        }

        new AlertDialog.Builder(this)
                .setTitle(isEditing ? "Sua san" : "Tao san")
                .setView(view)
                .setNegativeButton("Huy", null)
                .setPositiveButton(isEditing ? "Luu" : "Tao", (dialog, which) -> {
                    String name = readText(etName);
                    String type = readText(etType);
                    if (name.isEmpty() || type.isEmpty()) {
                        Toast.makeText(this, "Vui long nhap ten san va loai san.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isEditing) {
                        viewModel.updateField(field.getId(), new FieldUpdateRequest(
                                name, type, readText(etAddress), readText(etDescription),
                                readText(etCoverImage), readText(etStatus)));
                    } else {
                        viewModel.createField(new FieldCreateRequest(
                                name, type, readText(etAddress), readText(etDescription),
                                readText(etCoverImage), readText(etStatus)));
                    }
                })
                .show();
    }

    private void confirmDeleteField(Field field) {
        new AlertDialog.Builder(this)
                .setTitle("Xoa san")
                .setMessage("Ban co chac muon xoa san nay?")
                .setNegativeButton("Huy", null)
                .setPositiveButton("Xoa", (dialog, which) -> viewModel.deleteField(field.getId()))
                .show();
    }

    private void showTimeSlotManager(Field field) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_owner_time_slot_form, null);
        TextInputEditText etStartTime = view.findViewById(R.id.etOwnerSlotStartTime);
        TextInputEditText etEndTime = view.findViewById(R.id.etOwnerSlotEndTime);
        TextInputEditText etPrice = view.findViewById(R.id.etOwnerSlotPrice);
        TextInputEditText etStatus = view.findViewById(R.id.etOwnerSlotStatus);

        new AlertDialog.Builder(this)
                .setTitle("Tao khung gio")
                .setView(view)
                .setNegativeButton("Huy", null)
                .setPositiveButton("Tao", (dialog, which) -> {
                    Double price = parsePrice(readText(etPrice));
                    if (readText(etStartTime).isEmpty() || readText(etEndTime).isEmpty() || price == null) {
                        Toast.makeText(this, "Vui long nhap day du khung gio va gia.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.createTimeSlot(field.getId(), new TimeSlotCreateRequest(
                            readText(etStartTime), readText(etEndTime), price, readText(etStatus)));
                })
                .show();
    }

    private void showEditTimeSlot(Field field, TimeSlot timeSlot) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_owner_time_slot_form, null);
        TextInputEditText etStartTime = view.findViewById(R.id.etOwnerSlotStartTime);
        TextInputEditText etEndTime = view.findViewById(R.id.etOwnerSlotEndTime);
        TextInputEditText etPrice = view.findViewById(R.id.etOwnerSlotPrice);
        TextInputEditText etStatus = view.findViewById(R.id.etOwnerSlotStatus);

        etStartTime.setText(timeSlot.getStartTime());
        etEndTime.setText(timeSlot.getEndTime());
        etPrice.setText(String.valueOf(timeSlot.getPrice()));
        etStatus.setText(timeSlot.getStatus());

        new AlertDialog.Builder(this)
                .setTitle("Sua khung gio")
                .setView(view)
                .setNegativeButton("Huy", null)
                .setPositiveButton("Luu", (dialog, which) -> viewModel.updateTimeSlot(
                        field.getId(),
                        timeSlot.getId(),
                        new TimeSlotUpdateRequest(readText(etStartTime), readText(etEndTime),
                                parsePrice(readText(etPrice)), readText(etStatus))))
                .show();
    }

    private String readText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private Double parsePrice(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
