package com.example.timsanbong.ui.customer;

import android.app.DatePickerDialog;
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
    private AutoCompleteTextView actvTeam, actvBookingSelect, actvAgeRange, actvCostSharing, actvTimeSlot;
    private TextView chipBeginner, chipIntermediate, chipAdvanced;
    private TextInputLayout tilLocation, tilNeededMembers, tilPositions, tilPlayDate, tilTimeSlot, tilBookingId, tilBookingSelect;
    private TextInputEditText etPlayDate, etLocation, etDescription, etNeededMembers, etPositions, etBookingId;
    private androidx.appcompat.widget.SwitchCompat swHasField;
    private MaterialButton btnSubmit;

    private String selectedType = "FIND_OPPONENT";
    private String selectedSkill = "INTERMEDIATE";
    private String selectedDateIso = "";
    private Long selectedTeamId;
    private Long selectedBookingId;
    private final Map<String, Long> teamIdsByName = new HashMap<>();
    private final Map<String, com.example.timsanbong.data.model.Booking> bookingsByDisplay = new HashMap<>();
    private MatchViewModel matchViewModel;
    private TeamViewModel teamViewModel;
    private BookingViewModel bookingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_create_match_post);

        initViews();
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        teamViewModel = new ViewModelProvider(this).get(TeamViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        setupTabs();
        setupTeamDropdown();
        setupSkillChips();
        setupPickers();
        setupStaticDropdowns();
        setupObservers();

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());

        teamViewModel.loadMyTeams();
        bookingViewModel.loadMyBookings();
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
        tilTimeSlot = findViewById(R.id.tilTimeSlot);
        tilLocation = findViewById(R.id.tilLocation);
        tilNeededMembers = findViewById(R.id.tilNeededMembers);
        tilPositions = findViewById(R.id.tilPositions);
        tilBookingId = findViewById(R.id.tilBookingId);
        tilBookingSelect = findViewById(R.id.tilBookingSelect);

        etPlayDate = findViewById(R.id.etPlayDate);
        actvTimeSlot = findViewById(R.id.actvTimeSlot);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        etNeededMembers = findViewById(R.id.etNeededMembers);
        etPositions = findViewById(R.id.etPositions);
        actvAgeRange = findViewById(R.id.actvAgeRange);
        actvCostSharing = findViewById(R.id.actvCostSharing);
        actvBookingSelect = findViewById(R.id.actvBookingSelect);
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
        
        // Always show date/time for BOTH types
        tilPlayDate.setVisibility(View.VISIBLE);
        tilTimeSlot.setVisibility(View.VISIBLE);
        
        tilBookingSelect.setVisibility(hasField ? View.VISIBLE : View.GONE);
        tilLocation.setVisibility(hasField ? View.VISIBLE : View.GONE);
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

        ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, com.example.timsanbong.utils.Constants.TIME_SLOTS);
        actvTimeSlot.setAdapter(slotAdapter);
    }

    private void setupStaticDropdowns() {
        String[] ageOptions = {"Dưới 18 tuổi", "18-25 tuổi", "25-35 tuổi", "Trên 35 tuổi", "Mọi lứa tuổi"};
        actvAgeRange.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ageOptions));

        String[] costOptions = {"Chia đều (50-50)", "Thua trả hết", "Thắng trả ít", "Chủ kèo mời", "Tự thỏa thuận"};
        actvCostSharing.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, costOptions));

        actvBookingSelect.setOnItemClickListener((parent, view, position, id) -> {
            String display = parent.getItemAtPosition(position).toString();
            com.example.timsanbong.data.model.Booking b = bookingsByDisplay.get(display);
            if (b != null) {
                selectedBookingId = b.getId();
                etLocation.setText(b.getFieldName());
                selectedDateIso = b.getBookingDate();
                etPlayDate.setText(b.getBookingDate());
                String startTime = b.getStartTime().substring(0, 5);
                String endTime = b.getEndTime().substring(0, 5);
                actvTimeSlot.setText(startTime + " - " + endTime, false);
            }
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

        bookingViewModel.bookingsState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS || resource.data == null) {
                return;
            }
            bookingsByDisplay.clear();
            List<String> displays = new ArrayList<>();
            for (com.example.timsanbong.data.model.Booking b : resource.data) {
                if ("CANCELLED".equals(b.getStatus())) continue;
                String d = String.format("%s (%s %s)", b.getFieldName(), b.getBookingDate(), b.getStartTime());
                displays.add(d);
                bookingsByDisplay.put(d, b);
            }
            actvBookingSelect.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displays));
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
        String teamNameInput = actvTeam.getText().toString().trim();
        String timeSlot = actvTimeSlot.getText().toString().trim();
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        boolean hasField = swHasField.isChecked();

        // Basic common validation
        if (selectedDateIso.isEmpty() || timeSlot.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày và khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (hasField && location.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        String startTime = "";
        String endTime = "";
        if (timeSlot.contains(" - ")) {
            String[] parts = timeSlot.split(" - ");
            startTime = parts[0] + ":00";
            endTime = parts[1] + ":00";
        }

        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        MatchPostRequest request = new MatchPostRequest();
        request.setPostType(selectedType);
        
        // Ensure team name is sent if not empty
        if (!teamNameInput.isEmpty()) {
            request.setTeamName(teamNameInput);
            
            // Also include teamId if a formal team was selected from the dropdown
            if (teamIdsByName.containsKey(teamNameInput)) {
                request.setTeamId(teamIdsByName.get(teamNameInput));
            }
        } else {
            request.setTeamName(null);
            request.setTeamId(null);
        }

        request.setSkillLevel(selectedSkill);
        request.setMessage(description);
        request.setHasField(hasField);
        request.setFieldName(location);
        if (selectedBookingId != null) {
            request.setBookingId(selectedBookingId);
        }
        
        // Essential match info for BOTH now (date, timeStart, timeEnd)
        request.setDate(selectedDateIso);
        request.setTimeStart(startTime);
        request.setTimeEnd(endTime);

        if (MatchPost.TYPE_FIND_MEMBER.equals(selectedType)) {
            String needed = etNeededMembers.getText() != null ? etNeededMembers.getText().toString().trim() : "";
            if (!needed.isEmpty()) request.setNeededMembers(Integer.parseInt(needed));
            
            if (etPositions.getText() != null) request.setTargetPositions(etPositions.getText().toString().trim());
        }

        String age = actvAgeRange.getText().toString().trim();
        if (!age.isEmpty()) request.setAgeRange(age);

        String costSharing = actvCostSharing.getText().toString().trim();
        if (!costSharing.isEmpty()) request.setCostSharing(costSharing);

        matchViewModel.createMatchPost(request);
    }
}
