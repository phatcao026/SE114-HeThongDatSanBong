package com.example.timsanbong.ui.owner;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldCreateRequest;
import com.example.timsanbong.data.model.FieldUpdateRequest;
import com.example.timsanbong.data.model.TimeSlotCreateRequest;
import com.example.timsanbong.data.model.TimeSlotResponse;
import com.example.timsanbong.data.model.TimeSlotUpdateRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 1. Kế thừa từ Fragment thay vì AppCompatActivity
public class OwnerFieldsFragment extends Fragment {
    private OwnerFieldViewModel viewModel;
    private OwnerFieldAdapter fieldAdapter;
    private final List<Field> allFields = new ArrayList<>();
    private EditText etSearchFields;
    private TextView tvEmptyFields;
    private TextView tvAvailableCount;
    private TextView tvMaintenanceCount;
    private String searchQuery = "";

    // 2. Nạp giao diện fragment_owner_field_management vào container
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_field_management, container, false);
    }

    // 3. Thực hiện toàn bộ logic khởi tạo giao diện tại onViewCreated
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OwnerFieldViewModel.class);

        bindViews(view);
        setupRecyclerView(view);
        setupActions(view);
        setupObservers();

        // Bỏ trình quản lý thanh điều hướng cũ vì đã gom về Activity tổng
        // new OwnerNavBarManager(this, OwnerNavBarManager.ITEM_FIELDS).setup();

        viewModel.loadFields();
    }

    // 4. Ánh xạ các thành phần View qua biến gốc 'view' của Fragment
    private void bindViews(View view) {
        etSearchFields = view.findViewById(R.id.etSearchFields);
        tvEmptyFields = view.findViewById(R.id.tvOwnerEmptyFields);
        tvAvailableCount = view.findViewById(R.id.tvAvailableCount);
        tvMaintenanceCount = view.findViewById(R.id.tvMaintenanceCount);
    }

    private void setupRecyclerView(View view) {
        RecyclerView rvFields = view.findViewById(R.id.rvFieldList);
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

        // Thay 'this' bằng 'requireContext()'
        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFields.setAdapter(fieldAdapter);
    }

    private void setupActions(View view) {
        view.findViewById(R.id.fabAddField).setOnClickListener(v -> showFieldForm(null));
        etSearchFields.addTextChangedListener(new SimpleTextWatcher(value -> {
            searchQuery = value == null ? "" : value.trim();
            renderFields();
        }));
    }

    // 5. Sử dụng 'getViewLifecycleOwner()' để lắng nghe dữ liệu LiveData an toàn
    private void setupObservers() {
        viewModel.getFields().observe(getViewLifecycleOwner(), fields -> {
            allFields.clear();
            if (fields != null) {
                allFields.addAll(fields);
            }
            updateSummary();
            renderFields();
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty() && getContext() != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderFields() {
        List<Field> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.US);
        for (Field field : allFields) {
            if (query.isEmpty() || matchesField(field, query)) {
                filtered.add(field);
            }
        }
        fieldAdapter.submitList(filtered);
        if (tvEmptyFields != null) {
            tvEmptyFields.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean matchesField(Field field, String query) {
        return safe(field.getName()).toLowerCase(Locale.US).contains(query)
                || safe(field.getTypeLabel()).toLowerCase(Locale.US).contains(query)
                || safe(field.getStatus()).toLowerCase(Locale.US).contains(query)
                || safe(field.getAddress()).toLowerCase(Locale.US).contains(query)
                || safe(field.getDescription()).toLowerCase(Locale.US).contains(query);
    }

    private void updateSummary() {
        int available = 0;
        int maintenance = 0;
        for (Field field : allFields) {
            if ("MAINTENANCE".equalsIgnoreCase(field.getStatus())) {
                maintenance++;
            } else {
                available++;
            }
        }
        tvAvailableCount.setText("● " + available + " Available");
        tvMaintenanceCount.setText("● " + maintenance + " Maintenance");
        tvEmptyFields.setVisibility(allFields.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showFieldForm(Field field) {
        boolean editing = field != null;
        Dialog dialog = createDialog(R.layout.dialog_owner_field_form);

        TextView tvTitle = dialog.findViewById(R.id.tvFieldFormTitle);
        TextInputEditText etName = dialog.findViewById(R.id.etFieldName);
        AutoCompleteTextView etType = dialog.findViewById(R.id.etFieldType);
        TextInputEditText etAddress = dialog.findViewById(R.id.etFieldAddress);
        TextInputEditText etCoverImage = dialog.findViewById(R.id.etFieldCoverImage);
        TextInputEditText etDescription = dialog.findViewById(R.id.etFieldDescription);
        AutoCompleteTextView etStatus = dialog.findViewById(R.id.etFieldStatus);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelFieldForm);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveFieldForm);

        String[] types = {"FIVE_A_SIDE", "SEVEN_A_SIDE", "ELEVEN_A_SIDE"};
        String[] statuses = {"AVAILABLE", "MAINTENANCE", "BOOKED"};

        // Cấp ngữ cảnh yêu cầu qua hàm 'requireContext()' thay cho Activity context cũ
        etType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types));
        etStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses));

        tvTitle.setText(editing ? "Chỉnh sửa sân" : "Tạo sân mới");
        btnSave.setText(editing ? "Lưu thay đổi" : "Tạo sân");

        if (editing) {
            etName.setText(field.getName());
            etType.setText(normalizeTypeLabel(field.getType()), false);
            etAddress.setText(field.getAddress());
            etCoverImage.setText(field.getImageUrl());
            etDescription.setText(field.getDescription());
            etStatus.setText(normalizeStatus(field.getStatus()), false);
        } else {
            etType.setText(types[0], false);
            etStatus.setText("AVAILABLE", false);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = readText(etName);
            String type = readText(etType);
            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên sân và loại sân.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (editing) {
                viewModel.updateField(field.getId(), new FieldUpdateRequest(
                        name,
                        type,
                        readText(etAddress),
                        readText(etDescription),
                        readText(etCoverImage),
                        readText(etStatus)));
            } else {
                viewModel.createField(new FieldCreateRequest(
                        name,
                        type,
                        readText(etAddress),
                        readText(etDescription),
                        readText(etCoverImage),
                        readText(etStatus)));
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmDeleteField(Field field) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sân bóng")
                .setMessage("Bạn có chắc chắn muốn xóa sân \"" + field.getName() + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteField(field.getId()))
                .show();
    }

    private void showTimeSlotManager(Field field) {
        Dialog dialog = createDialog(R.layout.dialog_owner_time_slot_manager);
        TextView tvTitle = dialog.findViewById(R.id.tvOwnerTimeSlotManagerTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvOwnerTimeSlotManagerSubtitle);
        TextView tvEmpty = dialog.findViewById(R.id.tvOwnerTimeSlotEmpty);
        RecyclerView rvSlots = dialog.findViewById(R.id.rvOwnerTimeSlots);
        MaterialButton btnAdd = dialog.findViewById(R.id.btnAddOwnerTimeSlot);

        tvTitle.setText(field.getName());
        tvSubtitle.setText(field.getTypeLabel() + " · " + safe(field.getStatus()));

        OwnerTimeSlotAdapter adapter = new OwnerTimeSlotAdapter(new OwnerTimeSlotAdapter.Listener() {
            @Override
            public void onEditTimeSlot(TimeSlotResponse timeSlot) {
                showTimeSlotForm(field, timeSlot);
                dialog.dismiss();
            }

            @Override
            public void onDeleteTimeSlot(TimeSlotResponse timeSlot) {
                confirmDeleteTimeSlot(field, timeSlot);
                dialog.dismiss();
            }
        });

        rvSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSlots.setAdapter(adapter);
        adapter.submitList(field.getTimeSlots());
        tvEmpty.setVisibility(field.getTimeSlots() == null || field.getTimeSlots().isEmpty()
                ? View.VISIBLE : View.GONE);

        btnAdd.setOnClickListener(v -> {
            showTimeSlotForm(field, null);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTimeSlotForm(Field field, TimeSlotResponse timeSlot) {
        Dialog dialog = createDialog(R.layout.dialog_owner_time_slot_form);
        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        TextInputEditText etStartTime = dialog.findViewById(R.id.etOwnerSlotStartTime);
        TextInputEditText etEndTime = dialog.findViewById(R.id.etOwnerSlotEndTime);
        TextInputEditText etPrice = dialog.findViewById(R.id.etOwnerSlotPrice);
        AutoCompleteTextView etStatus = dialog.findViewById(R.id.etOwnerSlotStatus);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSave);

        String[] statuses = {"AVAILABLE", "PENDING", "BOOKED"};
        etStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses));

        boolean editing = timeSlot != null;
        tvTitle.setText(editing ? "Chỉnh sửa khung giờ" : "Thêm khung giờ");
        btnSave.setText(editing ? "Lưu khung giờ" : "Thêm khung giờ");

        if (editing) {
            etStartTime.setText(timeSlot.getStartTime());
            etEndTime.setText(timeSlot.getEndTime());
            etPrice.setText(timeSlot.getPrice() == null ? "" : String.valueOf(timeSlot.getPrice()));
            etStatus.setText(timeSlot.isAvailable() ? "AVAILABLE" : safe(timeSlot.getStatus()), false);
        } else {
            etStatus.setText("AVAILABLE", false);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String start = readText(etStartTime);
            String end = readText(etEndTime);
            BigDecimal price = parsePrice(readText(etPrice));
            if (start.isEmpty() || end.isEmpty() || price == null) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ giờ bắt đầu, giờ kết thúc và giá.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (editing) {
                viewModel.updateTimeSlot(
                        field.getId(),
                        timeSlot.getId(),
                        new TimeSlotUpdateRequest(start, end, price.doubleValue(), readText(etStatus)));
            } else {
                viewModel.createTimeSlot(
                        field.getId(),
                        new TimeSlotCreateRequest(start, end, price.doubleValue(), readText(etStatus)));
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmDeleteTimeSlot(Field field, TimeSlotResponse timeSlot) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa khung giờ")
                .setMessage("Bạn có chắc chắn muốn xóa khung giờ " + safe(timeSlot.getStartTime()) + " - " + safe(timeSlot.getEndTime()) + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteTimeSlot(field.getId(), timeSlot.getId()))
                .show();
    }

    // 6. Cập nhật hàm tạo Custom Dialog bằng requireContext()
    private Dialog createDialog(int layoutResId) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_TimSanBong);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private String readText(TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private String normalizeTypeLabel(String type) {
        if (type == null) {
            return "FIVE_A_SIDE";
        }
        String normalized = type.toUpperCase(Locale.US);
        if (normalized.contains("11")) {
            return "ELEVEN_A_SIDE";
        }
        if (normalized.contains("7")) {
            return "SEVEN_A_SIDE";
        }
        return "FIVE_A_SIDE";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "AVAILABLE";
        }
        return status.toUpperCase(Locale.US);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final java.util.function.Consumer<String> consumer;

        SimpleTextWatcher(java.util.function.Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            consumer.accept(s == null ? "" : s.toString());
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}