package com.example.timsanbong.ui.customer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.TeamResponse;
import com.example.timsanbong.data.repository.TeamRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class QuickFindBottomSheetFragment extends BottomSheetDialogFragment {

    private TextInputEditText etDate, etTime, etTimeEnd;
    private AutoCompleteTextView etTeamName, actvPostType, actvSkillLevel, actvHasField, actvAgeRange, actvCostSharing;
    private String selectedDateIso = "";
    private String selectedTimeStart = "";
    private String selectedTimeEnd = "";
    private TeamRepository teamRepository = new TeamRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_quick_find, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTeamName = view.findViewById(R.id.etTeamName);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        etTimeEnd = view.findViewById(R.id.etTimeEnd);
        actvPostType = view.findViewById(R.id.actvPostType);
        actvSkillLevel = view.findViewById(R.id.actvSkillLevel);
        actvHasField = view.findViewById(R.id.actvHasField);
        actvAgeRange = view.findViewById(R.id.actvAgeRange);
        actvCostSharing = view.findViewById(R.id.actvCostSharing);
        View btnSearch = view.findViewById(R.id.btnSearch);

        setupTeamAutoComplete();
        setupDropdowns();

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker(true));
        etTimeEnd.setOnClickListener(v -> showTimePicker(false));

        btnSearch.setOnClickListener(v -> {
            String teamName = etTeamName.getText() != null ? etTeamName.getText().toString().trim() : "";
            String ageRange = actvAgeRange.getText().toString().trim();
            if ("Tất cả".equals(ageRange)) ageRange = "";
            
            String postType = actvPostType.getText().toString();
            if ("Tìm đối thủ".equals(postType)) postType = "FIND_OPPONENT";
            else if ("Tìm cầu thủ".equals(postType)) postType = "FIND_MEMBER";
            else postType = "";

            String skillLevel = actvSkillLevel.getText().toString();
            if ("Mới chơi".equals(skillLevel)) skillLevel = "BEGINNER";
            else if ("Trung cấp".equals(skillLevel)) skillLevel = "INTERMEDIATE";
            else if ("Nâng cao".equals(skillLevel)) skillLevel = "ADVANCED";
            else skillLevel = "";

            String hasFieldStr = actvHasField.getText().toString();
            Boolean hasField = null;
            if ("Có".equals(hasFieldStr)) hasField = true;
            else if ("Không".equals(hasFieldStr)) hasField = false;

            String costSharing = actvCostSharing.getText().toString().trim();
            if ("Tất cả".equals(costSharing)) costSharing = "";

            Intent intent = new Intent(getContext(), QuickFindResultsActivity.class);
            intent.putExtra("teamName", teamName);
            intent.putExtra("date", selectedDateIso);
            intent.putExtra("time", selectedTimeStart);
            intent.putExtra("timeEnd", selectedTimeEnd);
            intent.putExtra("postType", postType);
            intent.putExtra("skillLevel", skillLevel);
            intent.putExtra("ageRange", ageRange);
            intent.putExtra("costSharing", costSharing);
            if (hasField != null) intent.putExtra("hasField", hasField);
            
            startActivity(intent);
            dismiss();
        });
    }

    private void setupDropdowns() {
        String[] postTypes = {"Tất cả", "Tìm đối thủ", "Tìm cầu thủ"};
        actvPostType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, postTypes));

        String[] skillLevels = {"Tất cả", "Mới chơi", "Trung cấp", "Nâng cao"};
        actvSkillLevel.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, skillLevels));

        String[] hasFieldOptions = {"Tất cả", "Có", "Không"};
        actvHasField.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, hasFieldOptions));

        String[] ageRanges = {"Tất cả", "Dưới 18 tuổi", "18-25 tuổi", "25-35 tuổi", "Trên 35 tuổi", "Mọi lứa tuổi"};
        actvAgeRange.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ageRanges));

        String[] costSharingOptions = {"Tất cả", "Chia đều (50-50)", "Đội thua trả", "Chủ kèo mời"};
        actvCostSharing.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, costSharingOptions));
    }

    private void setupTeamAutoComplete() {
        teamRepository.getMyTeams(getContext(), new com.example.timsanbong.utils.RepositoryCallback<List<TeamResponse>>() {
            @Override
            public void onSuccess(List<TeamResponse> data) {
                if (data != null && !data.isEmpty()) {
                    List<String> teamNames = new ArrayList<>();
                    for (TeamResponse team : data) {
                        teamNames.add(team.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, teamNames);
                    etTeamName.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                // Silently fail for suggestions
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            selectedDateIso = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(selectedDateIso);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String time = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute);
            if (isStart) {
                selectedTimeStart = time;
                etTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            } else {
                selectedTimeEnd = time;
                etTimeEnd.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }
}
