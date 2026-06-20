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

    private TextInputEditText etPlaystyle, etDate, etTime;
    private AutoCompleteTextView etTeamName;
    private String selectedDateIso = "";
    private String selectedTime = "";
    private TeamRepository teamRepository = new TeamRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_quick_find, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPlaystyle = view.findViewById(R.id.etPlaystyle);
        etTeamName = view.findViewById(R.id.etTeamName);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        View btnSearch = view.findViewById(R.id.btnSearch);

        setupTeamAutoComplete();

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnSearch.setOnClickListener(v -> {
            String playstyle = etPlaystyle.getText() != null ? etPlaystyle.getText().toString().trim() : "";
            String teamName = etTeamName.getText() != null ? etTeamName.getText().toString().trim() : "";
            
            // Note: date and time are already captured in variables when pickers close

            Intent intent = new Intent(getContext(), QuickFindResultsActivity.class);
            intent.putExtra("playstyle", playstyle);
            intent.putExtra("teamName", teamName);
            intent.putExtra("date", selectedDateIso);
            intent.putExtra("time", selectedTime);
            startActivity(intent);
            dismiss();
        });
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

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            selectedTime = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute);
            etTime.setText(selectedTime);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }
}
