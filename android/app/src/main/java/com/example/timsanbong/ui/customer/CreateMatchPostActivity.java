package com.example.timsanbong.ui.customer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.MatchPostRequest;
import com.example.timsanbong.data.model.TeamResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateMatchPostActivity extends AppCompatActivity {

    private android.widget.ImageView btnBack;
    private TextView tabFindOpponent, tabFindMember;
    private AutoCompleteTextView actvTeam;
    private TextView chipBeginner, chipIntermediate, chipAdvanced;
    private TextInputEditText etPlayDate, etPlayTime, etLocation, etDescription;
    private MaterialButton btnSubmit;

    private String selectedType = "FIND_OPPONENT";
    private String selectedSkill = "INTERMEDIATE";
    private String selectedDateIso = "";
    private Long selectedTeamId;
    private final Map<String, Long> teamIdsByName = new HashMap<>();
    private MatchViewModel matchViewModel;
    private TeamViewModel teamViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_create_match_post);

        initViews();
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        teamViewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        setupTabs();
        setupTeamDropdown();
        setupSkillChips();
        setupPickers();
        setupObservers();

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());

        teamViewModel.loadMyTeams();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabFindOpponent = findViewById(R.id.tabFindOpponent);
        tabFindMember = findViewById(R.id.tabFindMember);
        actvTeam = findViewById(R.id.actvTeam);
        chipBeginner = findViewById(R.id.chipBeginner);
        chipIntermediate = findViewById(R.id.chipIntermediate);
        chipAdvanced = findViewById(R.id.chipAdvanced);
        etPlayDate = findViewById(R.id.etPlayDate);
        etPlayTime = findViewById(R.id.etPlayTime);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupTabs() {
        tabFindOpponent.setOnClickListener(v -> {
            selectedType = "FIND_OPPONENT";
            tabFindOpponent.setBackgroundResource(R.drawable.bg_segment_active);
            tabFindOpponent.setTextColor(getColor(R.color.text_on_primary));
            tabFindOpponent.setTypeface(null, android.graphics.Typeface.BOLD);

            tabFindMember.setBackgroundResource(android.R.color.transparent);
            tabFindMember.setTextColor(getColor(R.color.text_secondary));
            tabFindMember.setTypeface(null, android.graphics.Typeface.NORMAL);
        });

        tabFindMember.setOnClickListener(v -> {
            selectedType = "FIND_MEMBER";
            tabFindMember.setBackgroundResource(R.drawable.bg_segment_active);
            tabFindMember.setTextColor(getColor(R.color.text_on_primary));
            tabFindMember.setTypeface(null, android.graphics.Typeface.BOLD);

            tabFindOpponent.setBackgroundResource(android.R.color.transparent);
            tabFindOpponent.setTextColor(getColor(R.color.text_secondary));
            tabFindOpponent.setTypeface(null, android.graphics.Typeface.NORMAL);
        });
    }

    private void setupTeamDropdown() {
        actvTeam.setOnItemClickListener((parent, view, position, id) -> {
            String teamName = parent.getItemAtPosition(position).toString();
            selectedTeamId = teamIdsByName.get(teamName);
        });
    }

    private void setupSkillChips() {
        View.OnClickListener listener = v -> {
            chipBeginner.setBackgroundResource(R.drawable.bg_chip_filter);
            chipBeginner.setTextColor(getColor(R.color.text_secondary));
            chipIntermediate.setBackgroundResource(R.drawable.bg_chip_filter);
            chipIntermediate.setTextColor(getColor(R.color.text_secondary));
            chipAdvanced.setBackgroundResource(R.drawable.bg_chip_filter);
            chipAdvanced.setTextColor(getColor(R.color.text_secondary));

            v.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            ((TextView) v).setTextColor(getColor(R.color.primary));

            if (v == chipBeginner) selectedSkill = "BEGINNER";
            else if (v == chipIntermediate) selectedSkill = "INTERMEDIATE";
            else if (v == chipAdvanced) selectedSkill = "ADVANCED";
        };

        chipBeginner.setOnClickListener(listener);
        chipIntermediate.setOnClickListener(listener);
        chipAdvanced.setOnClickListener(listener);
    }

    private void setupPickers() {
        etPlayDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                etPlayDate.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        etPlayTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) ->
                    etPlayTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute)),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
    }

    private void setupObservers() {
        teamViewModel.teamsState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS || resource.data == null) {
                return;
            }

            teamIdsByName.clear();
            List<String> teamNames = new ArrayList<>();
            for (TeamResponse team : resource.data) {
                teamNames.add(team.getName());
                teamIdsByName.put(team.getName(), team.getId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, teamNames);
            actvTeam.setAdapter(adapter);
        });

        matchViewModel.createMatchState.observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    btnSubmit.setEnabled(false);
                    break;
                case SUCCESS:
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Da tao bai dang thanh cong.", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ERROR:
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void validateAndSubmit() {
        String team = actvTeam.getText().toString().trim();
        String timeStart = etPlayTime.getText() != null ? etPlayTime.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        if (selectedDateIso.isEmpty() || timeStart.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!team.isEmpty() && !teamIdsByName.containsKey(team)) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            return;
        }

        selectedTeamId = team.isEmpty() ? null : teamIdsByName.get(team);

        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String message = description.isEmpty() ? location : description + "\n" + location;

        MatchPostRequest request = new MatchPostRequest();
        request.setPostType(selectedType);
        request.setTeamId(selectedTeamId);
        request.setDate(selectedDateIso);
        request.setTimeStart(timeStart);
        request.setTimeEnd(buildEndTime(timeStart));
        request.setSkillLevel(selectedSkill);
        request.setMessage(message);

        matchViewModel.createMatchPost(request);
    }

    private String buildEndTime(String timeStart) {
        String[] parts = timeStart.split(":");
        if (parts.length != 2) {
            return timeStart;
        }
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        minute += 90;
        hour = (hour + minute / 60) % 24;
        minute = minute % 60;
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }
}
