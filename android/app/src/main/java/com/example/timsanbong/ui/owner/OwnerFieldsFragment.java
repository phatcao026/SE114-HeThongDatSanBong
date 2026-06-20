package com.example.timsanbong.ui.owner;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private TextView tvClosedCount;
    private ActivityResultLauncher<String> createFieldImagePickerLauncher;
    private Uri createFieldImageUri;
    private LinearLayout createFieldNoImageContainer;
    private FrameLayout createFieldImagePreviewContainer;
    private ImageView createFieldImagePreview;
    private TextView createFieldNameError;
    private String createFieldSelectedType = "FIVE_A_SIDE";
    private String createFieldSelectedStatus = "AVAILABLE";
    private String searchQuery = "";
    private FilterMode filterMode = FilterMode.ALL;

    private enum FilterMode { ALL, AVAILABLE, MAINTENANCE, CLOSED }

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
        createFieldImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    createFieldImageUri = uri;
                    if (createFieldNoImageContainer != null) {
                        createFieldNoImageContainer.setVisibility(View.GONE);
                    }
                    if (createFieldImagePreviewContainer != null) {
                        createFieldImagePreviewContainer.setVisibility(View.VISIBLE);
                    }
                    if (createFieldImagePreview != null) {
                        Glide.with(this).load(uri).into(createFieldImagePreview);
                    }
                });

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
        tvClosedCount = view.findViewById(R.id.tvClosedCount);
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
        // Filter pills click handlers
        if (tvAvailableCount != null) {
            tvAvailableCount.setOnClickListener(v -> {
                filterMode = filterMode == FilterMode.AVAILABLE ? FilterMode.ALL : FilterMode.AVAILABLE;
                updateSummary();
                renderFields();
            });
        }
        if (tvMaintenanceCount != null) {
            tvMaintenanceCount.setOnClickListener(v -> {
                filterMode = filterMode == FilterMode.MAINTENANCE ? FilterMode.ALL : FilterMode.MAINTENANCE;
                updateSummary();
                renderFields();
            });
        }
        if (tvClosedCount != null) {
            tvClosedCount.setOnClickListener(v -> {
                filterMode = filterMode == FilterMode.CLOSED ? FilterMode.ALL : FilterMode.CLOSED;
                updateSummary();
                renderFields();
            });
        }
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
        // Apply status filter (Available / Maintenance / Closed)
        List<Field> statusFiltered = new ArrayList<>();
        for (Field f : filtered) {
            if (filterMode == FilterMode.AVAILABLE) {
                if (!"MAINTENANCE".equalsIgnoreCase(f.getStatus()) && !isFieldClosed(f)) statusFiltered.add(f);
            } else if (filterMode == FilterMode.MAINTENANCE) {
                if ("MAINTENANCE".equalsIgnoreCase(f.getStatus())) statusFiltered.add(f);
            } else if (filterMode == FilterMode.CLOSED) {
                if (isFieldClosed(f)) statusFiltered.add(f);
            } else {
                statusFiltered.add(f);
            }
        }
        fieldAdapter.submitList(statusFiltered);
        if (tvEmptyFields != null) {
            tvEmptyFields.setVisibility(statusFiltered.isEmpty() ? View.VISIBLE : View.GONE);
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
        int closed = 0;
        for (Field field : allFields) {
            if ("MAINTENANCE".equalsIgnoreCase(field.getStatus())) {
                maintenance++;
            } else if (isFieldClosed(field)) {
                closed++;
            } else {
                available++;
            }
        }
        if (getContext() != null) {
            tvAvailableCount.setText(getString(R.string.owner_available_pill, available));
            tvMaintenanceCount.setText(getString(R.string.owner_maintenance_pill, maintenance));
            if (tvClosedCount != null) tvClosedCount.setText(getString(R.string.owner_closed_pill, closed));
        }
        tvEmptyFields.setVisibility(allFields.isEmpty() ? View.VISIBLE : View.GONE);
        // Visual state for filter pills
        if (tvAvailableCount != null && tvMaintenanceCount != null && tvClosedCount != null) {
            // Reset all
            tvAvailableCount.setAlpha(1f);
            tvAvailableCount.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvMaintenanceCount.setAlpha(1f);
            tvMaintenanceCount.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvClosedCount.setAlpha(1f);
            tvClosedCount.setTypeface(null, android.graphics.Typeface.NORMAL);
            switch (filterMode) {
                case AVAILABLE:
                    tvAvailableCount.setAlpha(1f);
                    tvAvailableCount.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvMaintenanceCount.setAlpha(0.6f);
                    tvClosedCount.setAlpha(0.6f);
                    break;
                case MAINTENANCE:
                    tvMaintenanceCount.setAlpha(1f);
                    tvMaintenanceCount.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvAvailableCount.setAlpha(0.6f);
                    tvClosedCount.setAlpha(0.6f);
                    break;
                case CLOSED:
                    tvClosedCount.setAlpha(1f);
                    tvClosedCount.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvAvailableCount.setAlpha(0.6f);
                    tvMaintenanceCount.setAlpha(0.6f);
                    break;
                default:
                    // ALL
                    break;
            }
        }
    }

    private boolean isFieldClosed(Field f) {
        if (f == null) return false;
        // If status is explicitly set to CLOSED, respect it
        if ("CLOSED".equalsIgnoreCase(f.getStatus())) return true;
        // Otherwise, determine closed if there are time slots and none are available
        if (f.getTimeSlots() == null || f.getTimeSlots().isEmpty()) return false;
        for (TimeSlotResponse slot : f.getTimeSlots()) {
            if (slot == null) continue;
            if (slot.isAvailable()) return false;
        }
        return true;
    }

    private void showFieldForm(Field field) {
        if (field == null) {
            showCreateFieldForm();
            return;
        }

        // Use the dedicated edit bottom-sheet layout for editing
        Dialog dialog = createDialog(R.layout.dialog_owner_edit_field);

        TextView tvEditingFieldName = dialog.findViewById(R.id.tvEditingFieldName);
        View btnClose = dialog.findViewById(R.id.btnCloseEditField);
        EditText etName = dialog.findViewById(R.id.etEditFieldName);
        LinearLayout chipSan5 = dialog.findViewById(R.id.chipEditTypeSan5);
        LinearLayout chipSan7 = dialog.findViewById(R.id.chipEditTypeSan7);
        EditText etCapacity = dialog.findViewById(R.id.etEditCapacity);
        EditText etPrice = dialog.findViewById(R.id.etEditPricePerSlot);
        EditText etDescription = dialog.findViewById(R.id.etEditDescription);
        LinearLayout rbAvailable = dialog.findViewById(R.id.rbEditStatusAvailable);
        LinearLayout rbMaintenance = dialog.findViewById(R.id.rbEditStatusMaintenance);
        TextView ivCheckAvailable = dialog.findViewById(R.id.ivEditCheckAvailable);
        TextView ivCheckMaintenance = dialog.findViewById(R.id.ivEditCheckMaintenance);
        View btnCancel = dialog.findViewById(R.id.btnCancelEditField);
        View btnSubmit = dialog.findViewById(R.id.btnSubmitEditField);
        View btnDelete = dialog.findViewById(R.id.btnDeleteFieldFromEdit);
        FrameLayout framePreview = dialog.findViewById(R.id.frameEditImagePreview);
        ImageView ivPreview = dialog.findViewById(R.id.ivEditFieldImagePreview);
        View btnChangePhoto = dialog.findViewById(R.id.btnChangePhoto);

        tvEditingFieldName.setText(field.getName());
        etName.setText(field.getName());
        etDescription.setText(field.getDescription());
        etPrice.setText(field.getPricePerHour() > 0 ? String.valueOf((int) field.getPricePerHour()) : "");
        etCapacity.setText("");

        // Set type selection
        String t = normalizeTypeLabel(field.getType());
        boolean isSan5 = "FIVE_A_SIDE".equalsIgnoreCase(t);
        chipSan5.setBackgroundResource(isSan5 ? R.drawable.bg_type_chip_active : R.drawable.bg_type_chip_inactive);
        chipSan7.setBackgroundResource(!isSan5 ? R.drawable.bg_type_chip_active : R.drawable.bg_type_chip_inactive);
        chipSan5.setTag(Boolean.valueOf(isSan5));
        chipSan7.setTag(Boolean.valueOf(!isSan5));

        // Set status selection
        boolean isAvailable = !"MAINTENANCE".equalsIgnoreCase(field.getStatus());
        rbAvailable.setBackgroundResource(isAvailable ? R.drawable.bg_status_radio_active : R.drawable.bg_status_radio_inactive);
        rbMaintenance.setBackgroundResource(isAvailable ? R.drawable.bg_status_radio_inactive : R.drawable.bg_status_radio_active);
        ivCheckAvailable.setVisibility(isAvailable ? View.VISIBLE : View.GONE);
        ivCheckMaintenance.setVisibility(isAvailable ? View.GONE : View.VISIBLE);

        // Image preview if available
        if (field.getImageUrl() != null && !field.getImageUrl().trim().isEmpty()) {
            ivPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(field.getImageUrl()).into(ivPreview);
            if (framePreview != null) framePreview.setVisibility(View.VISIBLE);
        }

        View.OnClickListener dismiss = v -> dialog.dismiss();
        if (btnClose != null) btnClose.setOnClickListener(dismiss);
        if (btnCancel != null) btnCancel.setOnClickListener(dismiss);

        chipSan5.setOnClickListener(v -> {
            chipSan5.setBackgroundResource(R.drawable.bg_type_chip_active);
            chipSan7.setBackgroundResource(R.drawable.bg_type_chip_inactive);
            chipSan5.setTag(Boolean.TRUE);
            chipSan7.setTag(Boolean.FALSE);
        });
        chipSan7.setOnClickListener(v -> {
            chipSan7.setBackgroundResource(R.drawable.bg_type_chip_active);
            chipSan5.setBackgroundResource(R.drawable.bg_type_chip_inactive);
            chipSan7.setTag(Boolean.TRUE);
            chipSan5.setTag(Boolean.FALSE);
        });

        rbAvailable.setOnClickListener(v -> {
            rbAvailable.setBackgroundResource(R.drawable.bg_status_radio_active);
            rbMaintenance.setBackgroundResource(R.drawable.bg_status_radio_inactive);
            ivCheckAvailable.setVisibility(View.VISIBLE);
            ivCheckMaintenance.setVisibility(View.GONE);
        });
        rbMaintenance.setOnClickListener(v -> {
            rbAvailable.setBackgroundResource(R.drawable.bg_status_radio_inactive);
            rbMaintenance.setBackgroundResource(R.drawable.bg_status_radio_active);
            ivCheckAvailable.setVisibility(View.GONE);
            ivCheckMaintenance.setVisibility(View.VISIBLE);
        });

        if (btnChangePhoto != null && createFieldImagePickerLauncher != null) {
            btnChangePhoto.setOnClickListener(v -> createFieldImagePickerLauncher.launch("image/*"));
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = readText(etName);
                String type = isViewSelected(chipSan7) ? "SEVEN_A_SIDE" : "FIVE_A_SIDE";
                String description = readText(etDescription);
                String status = ivCheckAvailable.getVisibility() == View.VISIBLE ? "AVAILABLE" : "MAINTENANCE";
                String cover = ivPreview.getVisibility() == View.VISIBLE && field.getImageUrl() != null ? field.getImageUrl() : "";
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập tên sân.", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.updateField(field.getId(), new FieldUpdateRequest(
                        name,
                        type,
                        field.getAddress() == null ? "" : field.getAddress(),
                        description,
                        cover,
                        status));
                dialog.dismiss();
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa sân bóng")
                        .setMessage("Bạn có chắc chắn muốn xóa sân \"" + field.getName() + "\"?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (d, which) -> viewModel.deleteField(field.getId()))
                        .show();
            });
        }

        dialog.show();
    }

    private void showCreateFieldForm() {
        Dialog dialog = createDialog(R.layout.dialog_owner_create_field);
        createFieldImageUri = null;

        View closeButton = dialog.findViewById(R.id.btnCloseCreateField);
        View cancelButton = dialog.findViewById(R.id.btnCancelCreateField);
        View submitButton = dialog.findViewById(R.id.btnSubmitCreateField);
        View imagePickerContainer = dialog.findViewById(R.id.containerImagePicker);
        createFieldNoImageContainer = dialog.findViewById(R.id.llNoImage);
        createFieldImagePreviewContainer = dialog.findViewById(R.id.frameImagePreview);
        createFieldImagePreview = dialog.findViewById(R.id.ivFieldImagePreview);
        createFieldNameError = dialog.findViewById(R.id.tvFieldNameError);

        EditText etFieldName = dialog.findViewById(R.id.etFieldName);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        LinearLayout chipTypeSan5 = dialog.findViewById(R.id.chipTypeSan5);
        LinearLayout chipTypeSan7 = dialog.findViewById(R.id.chipTypeSan7);
        LinearLayout rbStatusAvailable = dialog.findViewById(R.id.rbStatusAvailable);
        LinearLayout rbStatusMaintenance = dialog.findViewById(R.id.rbStatusMaintenance);
        TextView ivCheckAvailable = dialog.findViewById(R.id.ivCheckAvailable);
        TextView ivCheckMaintenance = dialog.findViewById(R.id.ivCheckMaintenance);

        View.OnClickListener pickImage = v -> {
            if (createFieldImagePickerLauncher != null) {
                createFieldImagePickerLauncher.launch("image/*");
            }
        };

        if (imagePickerContainer != null) {
            imagePickerContainer.setOnClickListener(pickImage);
        }
        if (createFieldNoImageContainer != null) {
            createFieldNoImageContainer.setOnClickListener(pickImage);
        }
        if (createFieldImagePreviewContainer != null) {
            createFieldImagePreviewContainer.setOnClickListener(pickImage);
        }

        updateCreateFieldTypeSelection("FIVE_A_SIDE", chipTypeSan5, chipTypeSan7);
        updateCreateFieldStatusSelection("AVAILABLE", rbStatusAvailable, rbStatusMaintenance, ivCheckAvailable, ivCheckMaintenance);

        if (chipTypeSan5 != null) {
            chipTypeSan5.setOnClickListener(v -> updateCreateFieldTypeSelection("FIVE_A_SIDE", chipTypeSan5, chipTypeSan7));
        }
        if (chipTypeSan7 != null) {
            chipTypeSan7.setOnClickListener(v -> updateCreateFieldTypeSelection("SEVEN_A_SIDE", chipTypeSan5, chipTypeSan7));
        }

        if (rbStatusAvailable != null) {
            rbStatusAvailable.setOnClickListener(v -> updateCreateFieldStatusSelection("AVAILABLE", rbStatusAvailable, rbStatusMaintenance, ivCheckAvailable, ivCheckMaintenance));
        }
        if (rbStatusMaintenance != null) {
            rbStatusMaintenance.setOnClickListener(v -> updateCreateFieldStatusSelection("MAINTENANCE", rbStatusAvailable, rbStatusMaintenance, ivCheckAvailable, ivCheckMaintenance));
        }

        View.OnClickListener dismissListener = v -> dialog.dismiss();
        if (closeButton != null) {
            closeButton.setOnClickListener(dismissListener);
        }
        if (cancelButton != null) {
            cancelButton.setOnClickListener(dismissListener);
        }
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> {
                if (createFieldNameError != null) {
                    createFieldNameError.setVisibility(View.GONE);
                }

                String name = readText(etFieldName);
                if (name.isEmpty()) {
                    if (createFieldNameError != null) {
                        createFieldNameError.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(requireContext(), "Vui lòng nhập tên sân.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String description = readText(etDescription);

                String coverImage = createFieldImageUri == null ? "" : createFieldImageUri.toString();
                viewModel.createField(new FieldCreateRequest(
                        name,
                        createFieldSelectedType,
                        "",
                        description,
                        coverImage,
                        createFieldSelectedStatus));
                dialog.dismiss();
            });
        }

        dialog.setOnDismissListener(d -> clearCreateFieldDialogState());
        dialog.show();
    }

    private void updateCreateFieldTypeSelection(String type, LinearLayout chipTypeSan5, LinearLayout chipTypeSan7) {
        createFieldSelectedType = type;
        if (chipTypeSan5 == null || chipTypeSan7 == null) {
            return;
        }

        boolean isSan5 = "FIVE_A_SIDE".equals(type);
        boolean isSan7 = "SEVEN_A_SIDE".equals(type);

        chipTypeSan5.setBackgroundResource(isSan5 ? R.drawable.bg_type_chip_active : R.drawable.bg_type_chip_inactive);
        chipTypeSan7.setBackgroundResource(isSan7 ? R.drawable.bg_type_chip_active : R.drawable.bg_type_chip_inactive);

        updateTypeChipTexts(chipTypeSan5, isSan5);
        updateTypeChipTexts(chipTypeSan7, isSan7);
    }

    private void updateTypeChipTexts(LinearLayout chip, boolean selected) {
        if (chip == null) {
            return;
        }
        TextView titleView = (TextView) chip.getChildAt(0);
        TextView subtitleView = (TextView) chip.getChildAt(1);
        if (titleView != null) {
            titleView.setTextColor(getResources().getColor(selected ? android.R.color.white : R.color.slate_600, null));
        }
        if (subtitleView != null) {
            subtitleView.setTextColor(getResources().getColor(selected ? android.R.color.white : R.color.slate_400, null));
        }
    }

    private void updateCreateFieldStatusSelection(String status,
                                                  LinearLayout rbStatusAvailable,
                                                  LinearLayout rbStatusMaintenance,
                                                  TextView ivCheckAvailable,
                                                  TextView ivCheckMaintenance) {
        createFieldSelectedStatus = status;
        if (rbStatusAvailable == null || rbStatusMaintenance == null || ivCheckAvailable == null || ivCheckMaintenance == null) {
            return;
        }

        boolean availableSelected = "AVAILABLE".equals(status);
        rbStatusAvailable.setBackgroundResource(availableSelected ? R.drawable.bg_status_radio_active : R.drawable.bg_status_radio_inactive);
        rbStatusMaintenance.setBackgroundResource(availableSelected ? R.drawable.bg_status_radio_inactive : R.drawable.bg_status_radio_active);
        ivCheckAvailable.setVisibility(availableSelected ? View.VISIBLE : View.GONE);
        ivCheckMaintenance.setVisibility(availableSelected ? View.GONE : View.VISIBLE);
    }

    private String buildCreateFieldSupplementalInfo(String capacity, String price) {
        List<String> infoLines = new ArrayList<>();
        if (!capacity.isEmpty()) {
            infoLines.add("Sức chứa tối đa: " + capacity + " người");
        }
        if (!price.isEmpty()) {
            infoLines.add("Giá thuê / giờ: " + price + " ₫");
        }
        return TextUtils.join("\n", infoLines);
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception exception) {
            return null;
        }
    }

    private void clearCreateFieldDialogState() {
        createFieldImageUri = null;
        createFieldNoImageContainer = null;
        createFieldImagePreviewContainer = null;
        createFieldImagePreview = null;
        createFieldNameError = null;
        createFieldSelectedType = "FIVE_A_SIDE";
        createFieldSelectedStatus = "AVAILABLE";
    }

    private boolean isViewSelected(View v) {
        if (v == null) return false;
        Object tag = v.getTag();
        return tag instanceof Boolean && (Boolean) tag;
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
        tvSubtitle.setText(getString(R.string.owner_field_type_status_format, field.getTypeLabel(), safe(field.getStatus())));

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
            String selectedStatus = timeSlot.isAvailable() ? "AVAILABLE" : safe(timeSlot.getStatus());
            etStatus.setText(selectedStatus, false);
        } else {
            String defaultStatus = "AVAILABLE";
            etStatus.setText(defaultStatus, false);
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