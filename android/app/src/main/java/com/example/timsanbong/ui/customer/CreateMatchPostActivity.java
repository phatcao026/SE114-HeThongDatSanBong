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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class CreateMatchPostActivity extends AppCompatActivity {

    private android.widget.ImageView btnBack;
    private TextView tabFindOpponent, tabFindMember;
    private AutoCompleteTextView actvTeam;
    private TextView chipBeginner, chipIntermediate, chipAdvanced;
    private TextInputEditText etPlayDate, etPlayTime, etLocation, etDescription;
    private MaterialButton btnSubmit;
    
    private String selectedType = "FIND_OPPONENT";
    private String selectedSkill = "INTERMEDIATE";
    private MatchViewModel matchViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_create_match_post);

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

        btnBack.setOnClickListener(v -> finish());
        
        setupTabs();
        setupTeamDropdown();
        setupSkillChips();
        setupPickers();

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        matchViewModel.createMatchState.observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    btnSubmit.setEnabled(false);
                    break;
                case SUCCESS:
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Đã tạo bài đăng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ERROR:
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
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
        String[] teams = new String[]{"Bão Đông FC", "FC Nghệ Tĩnh", "Sài Gòn Warriors"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, teams);
        actvTeam.setAdapter(adapter);
    }

    private void setupSkillChips() {
        View.OnClickListener listener = v -> {
            // Reset all
            chipBeginner.setBackgroundResource(R.drawable.bg_chip_filter);
            chipBeginner.setTextColor(getColor(R.color.text_secondary));
            chipIntermediate.setBackgroundResource(R.drawable.bg_chip_filter);
            chipIntermediate.setTextColor(getColor(R.color.text_secondary));
            chipAdvanced.setBackgroundResource(R.drawable.bg_chip_filter);
            chipAdvanced.setTextColor(getColor(R.color.text_secondary));
            
            // Set selected
            v.setBackgroundResource(R.drawable.bg_chip_filter_selected);
            ((TextView)v).setTextColor(getColor(R.color.primary));
            
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
                String date = String.format("%02d/%02d/%04d", day, month + 1, year);
                etPlayDate.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        etPlayTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                String time = String.format("%02d:%02d", hour, minute);
                etPlayTime.setText(time);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
    }

    private void validateAndSubmit() {
        String team = actvTeam.getText().toString();
        String date = etPlayDate.getText() != null ? etPlayDate.getText().toString() : "";
        String time = etPlayTime.getText() != null ? etPlayTime.getText().toString() : "";
        String loc = etLocation.getText() != null ? etLocation.getText().toString() : "";
        
        if (team.isEmpty() || date.isEmpty() || time.isEmpty() || loc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.timsanbong.data.model.MatchPostRequest request = new com.example.timsanbong.data.model.MatchPostRequest();
        request.setPostType(selectedType);
        request.setTeamId(1L); // Mock team ID for now since we haven't wired teams
        request.setPlayDate(date);
        request.setPlayTime(time);
        request.setSkillLevel(selectedSkill);
        request.setDescription(etDescription.getText() != null ? etDescription.getText().toString() : "");
        // Missing location in Request DTO? We can just pass it as part of description or the backend handles field ID.
        
        matchViewModel.createMatchPost(request);
    }
}
