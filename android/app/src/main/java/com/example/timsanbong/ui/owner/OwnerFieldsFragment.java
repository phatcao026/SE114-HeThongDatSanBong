package com.example.timsanbong.ui.owner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OwnerFieldsFragment extends Fragment {
    private OwnerFieldViewModel viewModel;
    private OwnerFieldAdapter fieldAdapter;
    private OwnerTimeSlotAdapter timeSlotAdapter; 
    private RecyclerView rvFields;
    private final List<Field> allFields = new ArrayList<>();
    private EditText etSearchFields;
    private LinearLayout llEmptyState;
    private TextView tvAvailableCount, tvMaintenanceCount, tvClosedCount;
    private TextView tvManagementLabel;
    private View btnDateFieldFilter;
    private String selectedDate; 

    private ActivityResultLauncher<String> createFieldImagePickerLauncher;
    private Uri createFieldImageUri;
    private LinearLayout createFieldNoImageContainer;
    private FrameLayout createFieldImagePreviewContainer;
    private ImageView createFieldImagePreview;
    
    private ActivityResultLauncher<String> editFieldImagePickerLauncher;
    private Uri editFieldImageUri;
    private LinearLayout editFieldNoImageContainer;
    private FrameLayout editFieldImagePreviewContainer;
    private ImageView editFieldImagePreview;
    
    private String searchQuery = "";
    private FilterMode filterMode = FilterMode.ALL;

    private enum FilterMode { ALL, AVAILABLE, MAINTENANCE, CLOSED }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_field_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OwnerFieldViewModel.class);
        
        Calendar cal = Calendar.getInstance();
        selectedDate = String.format(Locale.US, "%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

        createFieldImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    createFieldImageUri = uri;
                    if (createFieldNoImageContainer != null) createFieldNoImageContainer.setVisibility(View.GONE);
                    if (createFieldImagePreviewContainer != null) createFieldImagePreviewContainer.setVisibility(View.VISIBLE);
                    if (createFieldImagePreview != null) Glide.with(this).load(uri).into(createFieldImagePreview);
                });

        editFieldImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    editFieldImageUri = uri;
                    if (editFieldNoImageContainer != null) editFieldNoImageContainer.setVisibility(View.GONE);
                    if (editFieldImagePreviewContainer != null) editFieldImagePreviewContainer.setVisibility(View.VISIBLE);
                    if (editFieldImagePreview != null) Glide.with(this).load(uri).into(editFieldImagePreview);
                });

        bindViews(view);
        setupRecyclerView(view);
        setupActions(view);
        setupObservers();

        viewModel.loadFields(selectedDate);
    }

    private void bindViews(View view) {
        etSearchFields = view.findViewById(R.id.etSearchFields);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        tvAvailableCount = view.findViewById(R.id.tvAvailableCount);
        tvMaintenanceCount = view.findViewById(R.id.tvMaintenanceCount);
        tvClosedCount = view.findViewById(R.id.tvClosedCount);
        tvManagementLabel = view.findViewById(R.id.tvManagementLabel);
        btnDateFieldFilter = view.findViewById(R.id.btnDateFieldFilter);
        updateDateLabel();
    }

    private void updateDateLabel() {
        if (tvManagementLabel != null) {
            Calendar now = Calendar.getInstance();
            String today = String.format(Locale.US, "%04d-%02d-%02d",
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
            
            if (selectedDate.equals(today)) {
                tvManagementLabel.setText("Tình trạng đặt sân hôm nay");
            } else {
                try {
                    String[] parts = selectedDate.split("-");
                    tvManagementLabel.setText("Tình trạng sân ngày " + parts[2] + "/" + parts[1]);
                } catch (Exception e) {
                    tvManagementLabel.setText("Tình trạng sân ngày " + selectedDate);
                }
            }
        }
    }

    private void setupRecyclerView(View view) {
        rvFields = view.findViewById(R.id.rvFieldList);
        fieldAdapter = new OwnerFieldAdapter(new OwnerFieldAdapter.Listener() {
            @Override public void onEditField(Field field) { showFieldForm(field); }
            @Override public void onDeleteField(Field field) { confirmDeleteField(field); }
            @Override public void onManageTimeSlots(Field field) { showTimeSlotManager(field); }
        });
        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFields.setAdapter(fieldAdapter);
    }

    private void setupActions(View view) {
        view.findViewById(R.id.fabAddField).setOnClickListener(v -> showFieldForm(null));
        
        if (btnDateFieldFilter != null) {
            btnDateFieldFilter.setOnClickListener(v -> showDatePicker());
        }

        etSearchFields.addTextChangedListener(new SimpleTextWatcher(value -> {
            searchQuery = value == null ? "" : value.trim();
            renderFields();
        }));
        
        tvAvailableCount.setOnClickListener(v -> { filterMode = (filterMode == FilterMode.AVAILABLE) ? FilterMode.ALL : FilterMode.AVAILABLE; updateSummaryUI(); renderFields(); });
        tvMaintenanceCount.setOnClickListener(v -> { filterMode = (filterMode == FilterMode.MAINTENANCE) ? FilterMode.ALL : FilterMode.MAINTENANCE; updateSummaryUI(); renderFields(); });
        tvClosedCount.setOnClickListener(v -> { filterMode = (filterMode == FilterMode.CLOSED) ? FilterMode.ALL : FilterMode.CLOSED; updateSummaryUI(); renderFields(); });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        try {
            String[] parts = selectedDate.split("-");
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        } catch (Exception e) {}

        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            updateDateLabel();
            viewModel.loadFields(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            View loadingBar = getActivity() != null ? getActivity().findViewById(R.id.ownerLoadingBar) : null;
            if (loadingBar != null) loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (getView() != null) getView().setAlpha(isLoading ? 0.5f : 1.0f);
        });

        viewModel.getFields().observe(getViewLifecycleOwner(), fields -> {
            allFields.clear();
            if (fields != null) allFields.addAll(fields);
            updateSummaryUI();
            renderFields();
        });

        viewModel.getCurrentFieldAvailability().observe(getViewLifecycleOwner(), slots -> {
            if (slots != null && timeSlotAdapter != null) {
                timeSlotAdapter.submitList(slots); 
            }
        });
        
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummaryUI() {
        int available = 0, maintenance = 0, closed = 0;
        for (Field field : allFields) {
            if ("MAINTENANCE".equalsIgnoreCase(field.getStatus())) {
                maintenance++;
            } else {
                boolean hasAnyAvailable = false;
                if (field.getTimeSlots() != null && !field.getTimeSlots().isEmpty()) {
                    for (TimeSlotResponse ts : field.getTimeSlots()) {
                        if (ts.isAvailable()) { hasAnyAvailable = true; break; }
                    }
                    if (hasAnyAvailable) available++;
                    else closed++;
                } else {
                    // Nếu không có thông tin khung giờ, mặc định là có thể khả dụng nếu status không phải CLOSED
                    if (!"CLOSED".equalsIgnoreCase(field.getStatus())) available++;
                    else closed++;
                }
            }
        }
        
        tvAvailableCount.setText(getString(R.string.owner_available_pill, available));
        tvMaintenanceCount.setText(getString(R.string.owner_maintenance_pill, maintenance));
        tvClosedCount.setText(getString(R.string.owner_closed_pill, closed));

        tvAvailableCount.setAlpha(filterMode == FilterMode.AVAILABLE || filterMode == FilterMode.ALL ? 1f : 0.4f);
        tvMaintenanceCount.setAlpha(filterMode == FilterMode.MAINTENANCE || filterMode == FilterMode.ALL ? 1f : 0.4f);
        tvClosedCount.setAlpha(filterMode == FilterMode.CLOSED || filterMode == FilterMode.ALL ? 1f : 0.4f);
    }

    private void renderFields() {
        List<Field> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.US);
        for (Field field : allFields) {
            boolean matchesSearch = query.isEmpty() || field.getName().toLowerCase(Locale.US).contains(query);
            if (!matchesSearch) continue;

            boolean isMaintenance = "MAINTENANCE".equalsIgnoreCase(field.getStatus());
            boolean hasAvailable = false;
            boolean hasTimeSlotData = field.getTimeSlots() != null && !field.getTimeSlots().isEmpty();
            
            if (hasTimeSlotData) {
                for (TimeSlotResponse ts : field.getTimeSlots()) {
                    if (ts.isAvailable()) { hasAvailable = true; break; }
                }
            } else {
                // Giả định khả dụng nếu không có data khung giờ và status là AVAILABLE
                hasAvailable = "AVAILABLE".equalsIgnoreCase(field.getStatus()) || field.getStatus() == null;
            }
            
            boolean isClosed = !isMaintenance && !hasAvailable;

            if (filterMode == FilterMode.AVAILABLE && hasAvailable && !isMaintenance) filtered.add(field);
            else if (filterMode == FilterMode.MAINTENANCE && isMaintenance) filtered.add(field);
            else if (filterMode == FilterMode.CLOSED && isClosed) filtered.add(field);
            else if (filterMode == FilterMode.ALL) filtered.add(field);
        }
        fieldAdapter.submitList(filtered);
        llEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showTimeSlotManager(Field field) {
        Dialog dialog = createDialog(R.layout.dialog_owner_time_slot_manager);
        ((TextView) dialog.findViewById(R.id.tvOwnerTimeSlotManagerTitle)).setText(field.getName());
        
        RecyclerView rvSlots = dialog.findViewById(R.id.rvOwnerTimeSlots);
        timeSlotAdapter = new OwnerTimeSlotAdapter(new OwnerTimeSlotAdapter.Listener() {
            @Override public void onEditTimeSlot(TimeSlotResponse ts) { showTimeSlotForm(field, ts); dialog.dismiss(); }
            @Override public void onDeleteTimeSlot(TimeSlotResponse ts) { confirmDeleteTimeSlot(field, ts); dialog.dismiss(); }
        });
        rvSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSlots.setAdapter(timeSlotAdapter);
        
        viewModel.loadFieldAvailability(field.getId(), selectedDate);

        dialog.findViewById(R.id.btnAddOwnerTimeSlot).setOnClickListener(v -> { showTimeSlotForm(field, null); dialog.dismiss(); });
        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showFieldForm(Field field) {
        if (field == null) { showCreateFieldForm(); return; }
        Dialog dialog = createDialog(R.layout.dialog_owner_edit_field);
        
        // Initialize image picker views
        editFieldNoImageContainer = dialog.findViewById(R.id.llEditNoImage);
        editFieldImagePreviewContainer = dialog.findViewById(R.id.frameEditImagePreview);
        editFieldImagePreview = dialog.findViewById(R.id.ivEditFieldImagePreview);
        editFieldImageUri = null; // Ensure it's reset
        
        dialog.findViewById(R.id.btnChangePhoto).setOnClickListener(v -> editFieldImagePickerLauncher.launch("image/*"));
        
        // Pre-fill image if exists
        if (field.getImageUrl() != null && !field.getImageUrl().isEmpty() && !field.getImageUrl().equals("null")) {
            editFieldImagePreviewContainer.setVisibility(View.VISIBLE);
            editFieldNoImageContainer.setVisibility(View.GONE);
            editFieldImagePreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(field.getImageUrl()).into(editFieldImagePreview);
        } else {
            editFieldNoImageContainer.setVisibility(View.VISIBLE);
            editFieldImagePreviewContainer.setVisibility(View.GONE);
            // Even if no image, user should be able to click the container to pick an image
            // Assuming containerEditImagePicker is the ID of the container
            dialog.findViewById(R.id.containerEditImagePicker).setOnClickListener(v -> editFieldImagePickerLauncher.launch("image/*"));
        }

        EditText etName = dialog.findViewById(R.id.etEditFieldName);
        EditText etDescription = dialog.findViewById(R.id.etEditDescription);
        etName.setText(field.getName());
        etDescription.setText(field.getDescription());
        ((TextView) dialog.findViewById(R.id.tvEditingFieldName)).setText(field.getName());
        
        // Handle field type selection
        final String[] fieldType = {field.getType()};
        View chip5 = dialog.findViewById(R.id.chipEditTypeSan5);
        View chip7 = dialog.findViewById(R.id.chipEditTypeSan7);
        
        if ("FIVE_A_SIDE".equalsIgnoreCase(fieldType[0])) chip5.setBackgroundResource(R.drawable.bg_type_chip_active);
        else chip7.setBackgroundResource(R.drawable.bg_type_chip_active);
        
        chip5.setOnClickListener(v -> {
            fieldType[0] = "FIVE_A_SIDE";
            v.setBackgroundResource(R.drawable.bg_type_chip_active);
            chip7.setBackgroundResource(R.drawable.bg_type_chip_inactive);
        });
        chip7.setOnClickListener(v -> {
            fieldType[0] = "SEVEN_A_SIDE";
            v.setBackgroundResource(R.drawable.bg_type_chip_active);
            chip5.setBackgroundResource(R.drawable.bg_type_chip_inactive);
        });

        // Handle status selection
        final String[] status = {field.getStatus()};
        View rbAvailable = dialog.findViewById(R.id.rbEditStatusAvailable);
        View rbMaintenance = dialog.findViewById(R.id.rbEditStatusMaintenance);
        TextView ivCheckAvailable = dialog.findViewById(R.id.ivEditCheckAvailable);
        TextView ivCheckMaintenance = dialog.findViewById(R.id.ivEditCheckMaintenance);

        if ("MAINTENANCE".equalsIgnoreCase(status[0])) {
            rbMaintenance.setBackgroundResource(R.drawable.bg_status_radio_active);
            ivCheckMaintenance.setVisibility(View.VISIBLE);
            rbAvailable.setBackgroundResource(R.drawable.bg_status_radio_inactive);
            ivCheckAvailable.setVisibility(View.GONE);
        } else {
            rbAvailable.setBackgroundResource(R.drawable.bg_status_radio_active);
            ivCheckAvailable.setVisibility(View.VISIBLE);
            rbMaintenance.setBackgroundResource(R.drawable.bg_status_radio_inactive);
            ivCheckMaintenance.setVisibility(View.GONE);
        }

        rbAvailable.setOnClickListener(v -> {
            status[0] = "AVAILABLE";
            v.setBackgroundResource(R.drawable.bg_status_radio_active);
            ivCheckAvailable.setVisibility(View.VISIBLE);
            rbMaintenance.setBackgroundResource(R.drawable.bg_status_radio_inactive);
            ivCheckMaintenance.setVisibility(View.GONE);
        });
        rbMaintenance.setOnClickListener(v -> {
            status[0] = "MAINTENANCE";
            v.setBackgroundResource(R.drawable.bg_status_radio_active);
            ivCheckMaintenance.setVisibility(View.VISIBLE);
            rbAvailable.setBackgroundResource(R.drawable.bg_status_radio_inactive);
            ivCheckAvailable.setVisibility(View.GONE);
        });
        
        dialog.findViewById(R.id.btnSubmitEditField).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            // Use new image URI if selected, otherwise fallback to existing
            String imageUrl = (editFieldImageUri != null) ? editFieldImageUri.toString() : field.getImageUrl();
            viewModel.updateField(field.getId(), new FieldUpdateRequest(name, fieldType[0], field.getAddress(), etDescription.getText().toString(), imageUrl, status[0]));
            dialog.dismiss();
        });
        dialog.findViewById(R.id.btnDeleteFieldFromEdit).setOnClickListener(v -> { confirmDeleteField(field); dialog.dismiss(); });
        dialog.findViewById(R.id.btnCloseEditField).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCancelEditField).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showCreateFieldForm() {
        Dialog dialog = createDialog(R.layout.dialog_owner_create_field);
        
        // Initialize image picker views
        createFieldNoImageContainer = dialog.findViewById(R.id.llNoImage);
        createFieldImagePreviewContainer = dialog.findViewById(R.id.frameImagePreview);
        createFieldImagePreview = dialog.findViewById(R.id.ivFieldImagePreview);
        createFieldImageUri = null;
        
        dialog.findViewById(R.id.containerImagePicker).setOnClickListener(v -> createFieldImagePickerLauncher.launch("image/*"));

        EditText etName = dialog.findViewById(R.id.etFieldName);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        
        // Handle field type selection
        final String[] fieldType = {"FIVE_A_SIDE"};
        dialog.findViewById(R.id.chipTypeSan5).setOnClickListener(v -> {
            fieldType[0] = "FIVE_A_SIDE";
            v.setBackgroundResource(R.drawable.bg_type_chip_active);
            dialog.findViewById(R.id.chipTypeSan7).setBackgroundResource(R.drawable.bg_type_chip_inactive);
        });
        dialog.findViewById(R.id.chipTypeSan7).setOnClickListener(v -> {
            fieldType[0] = "SEVEN_A_SIDE";
            v.setBackgroundResource(R.drawable.bg_type_chip_active);
            dialog.findViewById(R.id.chipTypeSan5).setBackgroundResource(R.drawable.bg_type_chip_inactive);
        });

        // Handle status selection
        final String[] status = {"AVAILABLE"};
        dialog.findViewById(R.id.rbStatusAvailable).setOnClickListener(v -> {
            status[0] = "AVAILABLE";
            v.setBackgroundResource(R.drawable.bg_status_radio_active);
            dialog.findViewById(R.id.ivCheckAvailable).setVisibility(View.VISIBLE);
            dialog.findViewById(R.id.rbStatusMaintenance).setBackgroundResource(R.drawable.bg_status_radio_inactive);
            dialog.findViewById(R.id.ivCheckMaintenance).setVisibility(View.GONE);
        });
        dialog.findViewById(R.id.rbStatusMaintenance).setOnClickListener(v -> {
            status[0] = "MAINTENANCE";
            v.setBackgroundResource(R.drawable.bg_status_radio_active);
            dialog.findViewById(R.id.ivCheckMaintenance).setVisibility(View.VISIBLE);
            dialog.findViewById(R.id.rbStatusAvailable).setBackgroundResource(R.drawable.bg_status_radio_inactive);
            dialog.findViewById(R.id.ivCheckAvailable).setVisibility(View.GONE);
        });

        dialog.findViewById(R.id.btnSubmitCreateField).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                dialog.findViewById(R.id.tvFieldNameError).setVisibility(View.VISIBLE);
                return;
            }
            // Note: Image upload logic needs to be implemented. For now, passing image URL as empty.
            viewModel.createField(new FieldCreateRequest(name, fieldType[0], "", etDescription.getText().toString(), "", status[0]));
            dialog.dismiss();
        });
        dialog.findViewById(R.id.btnCancelCreateField).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCloseCreateField).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showTimeSlotForm(Field field, TimeSlotResponse ts) {
        Dialog dialog = createDialog(R.layout.dialog_owner_time_slot_form);
        EditText etStart = dialog.findViewById(R.id.etOwnerSlotStartTime);
        EditText etEnd = dialog.findViewById(R.id.etOwnerSlotEndTime);
        EditText etPrice = dialog.findViewById(R.id.etOwnerSlotPrice);
        if (ts != null) { 
            etStart.setText(ts.getStartTime()); 
            etEnd.setText(ts.getEndTime()); 
            etPrice.setText(String.valueOf(ts.getPrice().intValue())); 
        }
        dialog.findViewById(R.id.btnSave).setOnClickListener(v -> {
            try {
                double p = Double.parseDouble(etPrice.getText().toString());
                if (ts != null) viewModel.updateTimeSlot(field.getId(), ts.getId(), new TimeSlotUpdateRequest(etStart.getText().toString(), etEnd.getText().toString(), p, "AVAILABLE"));
                else viewModel.createTimeSlot(field.getId(), new TimeSlotCreateRequest(etStart.getText().toString(), etEnd.getText().toString(), p, "AVAILABLE"));
                dialog.dismiss();
            } catch (Exception e) {}
        });
        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void confirmDeleteField(Field field) {
        new AlertDialog.Builder(requireContext()).setTitle("Xóa sân").setMessage("Xóa \"" + field.getName() + "\"?").setPositiveButton("Xóa", (d, w) -> viewModel.deleteField(field.getId())).setNegativeButton("Hủy", null).show();
    }

    private void confirmDeleteTimeSlot(Field field, TimeSlotResponse ts) {
        new AlertDialog.Builder(requireContext()).setTitle("Xóa khung giờ").setMessage("Xóa " + ts.getStartTime() + "?").setPositiveButton("Xóa", (d, w) -> viewModel.deleteTimeSlot(field.getId(), ts.getId())).setNegativeButton("Hủy", null).show();
    }

    private Dialog createDialog(int resId) {
        Dialog d = new Dialog(requireContext(), R.style.Theme_TimSanBong);
        d.setContentView(resId);
        d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return d;
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> c;
        SimpleTextWatcher(java.util.function.Consumer<String> c) { this.c = c; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { c.accept(s.toString()); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
