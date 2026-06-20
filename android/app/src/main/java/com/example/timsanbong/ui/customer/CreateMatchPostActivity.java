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
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MatchPostRequest;
import com.example.timsanbong.data.model.TeamResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputLayout tilLocation, tilNeededMembers, tilPositions, tilPlayDate, tilPlayTime, tilEndTime, tilBookingId;
    private TextInputEditText etPlayDate, etPlayTime, etEndTime, etLocation, etDescription, etNeededMembers, etPositions, etAgeRange, etCostSharing, etBookingId;
    private androidx.appcompat.widget.SwitchCompat swHasField;
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
        
        tilPlayDate = findViewById(R.id.tilPlayDate);
        tilPlayTime = findViewById(R.id.tilPlayTime);
        tilEndTime = findViewById(R.id.tilEndTime);
        tilLocation = findViewById(R.id.tilLocation);
        tilNeededMembers = findViewById(R.id.tilNeededMembers);
        tilPositions = findViewById(R.id.tilPositions);
        tilBookingId = findViewById(R.id.tilBookingId);

        etPlayDate = findViewById(R.id.etPlayDate);
        etPlayTime = findViewById(R.id.etPlayTime);
        etEndTime = findViewById(R.id.etEndTime);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        etNeededMembers = findViewById(R.id.etNeededMembers);
        etPositions = findViewById(R.id.etPositions);
        etAgeRange = findViewById(R.id.etAgeRange);
        etCostSharing = findViewById(R.id.etCostSharing);
        etBookingId = findViewById(R.id.etBookingId);
        swHasField = findViewById(R.id.swHasField);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Initial visibilities
        updateInputVisibilities();
        
        swHasField.setOnCheckedChangeListener((v, isChecked) -> updateInputVisibilities());
    }

    private void updateInputVisibilities() {
        boolean isFindMember = MatchPost.TYPE_FIND_MEMBER.equals(selectedType);
        boolean hasField = swHasField.isChecked();

        tilNeededMembers.setVisibility(isFindMember ? View.VISIBLE : View.GONE);
        tilPositions.setVisibility(isFindMember ? View.VISIBLE : View.GONE);
        
        // Show date/time for BOTH types now as per request
        tilPlayDate.setVisibility(View.VISIBLE);
        tilPlayTime.setVisibility(View.VISIBLE);
        tilEndTime.setVisibility(isFindMember ? View.VISIBLE : View.GONE);
        
        tilLocation.setVisibility(hasField ? View.VISIBLE : View.GONE);
        // BookingId remains hidden as requested "xoa cai booking id di"
        tilBookingId.setVisibility(View.GONE);
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
            
            updateInputVisibilities();
        });

        tabFindMember.setOnClickListener(v -> {
            selectedType = "FIND_MEMBER";
            tabFindMember.setBackgroundResource(R.drawable.bg_segment_active);
            tabFindMember.setTextColor(getColor(R.color.text_on_primary));
            tabFindMember.setTypeface(null, android.graphics.Typeface.BOLD);

            tabFindOpponent.setBackgroundResource(android.R.color.transparent);
            tabFindOpponent.setTextColor(getColor(R.color.text_secondary));
            tabFindOpponent.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            updateInputVisibilities();
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
            new TimePickerDialog(this, (view, hour, minute) -> {
                String time = String.format(Locale.US, "%02d:%02d", hour, minute);
                etPlayTime.setText(time);
                // Pre-fill end time (90 mins later)
                etEndTime.setText(buildEndTime(time));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        etEndTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) ->
                    etEndTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute)),
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
                    Toast.makeText(this, "Đã tạo bài đăng thành công.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
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
        String teamName = actvTeam.getText().toString().trim();
        String timeStart = etPlayTime.getText() != null ? etPlayTime.getText().toString().trim() : "";
        String timeEnd = etEndTime.getText() != null ? etEndTime.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        boolean hasField = swHasField.isChecked();

        // Basic common validation
        if (selectedDateIso.isEmpty() || timeStart.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày và giờ đá", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (hasField && location.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Team ID resolution
        if (teamIdsByName.containsKey(teamName)) {
            selectedTeamId = teamIdsByName.get(teamName);
        } else {
            selectedTeamId = null;
        }

        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        MatchPostRequest request = new MatchPostRequest();
        request.setPostType(selectedType);
        
        // Use teamId if available, otherwise just use the typed teamName
        if (teamIdsByName.containsKey(teamName)) {
            request.setTeamId(teamIdsByName.get(teamName));
        } else {
            request.setTeamName(teamName.isEmpty() ? null : teamName);
        }

        request.setSkillLevel(selectedSkill);
        request.setMessage(description);
        request.setHasField(hasField);
        request.setFieldName(location);
        
        // Essential match info for BOTH now (date, timeStart)
        request.setDate(selectedDateIso);
        request.setTimeStart(timeStart + ":00");

        if (MatchPost.TYPE_FIND_MEMBER.equals(selectedType)) {
            // FIND_MEMBER needs explicit timeEnd
            String finalEndTime = timeEnd.isEmpty() ? buildEndTime(timeStart) : timeEnd;
            request.setTimeEnd(finalEndTime + ":00");
            
            String needed = etNeededMembers.getText() != null ? etNeededMembers.getText().toString().trim() : "";
            if (!needed.isEmpty()) request.setNeededMembers(Integer.parseInt(needed));
            
            if (etPositions.getText() != null) request.setTargetPositions(etPositions.getText().toString().trim());
        } else {
            // FIND_OPPONENT might not strictly need timeEnd in JSON but good to have if provided
            if (!timeEnd.isEmpty()) request.setTimeEnd(timeEnd + ":00");
            else request.setTimeEnd(buildEndTime(timeStart) + ":00");
        }

        if (etAgeRange.getText() != null) request.setAgeRange(etAgeRange.getText().toString().trim());
        if (etCostSharing.getText() != null) request.setCostSharing(etCostSharing.getText().toString().trim());

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
