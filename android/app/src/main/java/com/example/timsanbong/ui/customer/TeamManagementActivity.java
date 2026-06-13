package com.example.timsanbong.ui.customer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TeamManagementActivity extends AppCompatActivity {

    private android.widget.ImageView btnBack;
    private TextView tabMyTeams, tabInvitations;
    private androidx.recyclerview.widget.RecyclerView rvTeams, rvInvitations;
    private TextView tvEmptyTeams, tvEmptyInvitations;
    private FloatingActionButton fabCreateTeam;
    private TeamViewModel teamViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_team_management);

        btnBack = findViewById(R.id.btnBack);
        tabMyTeams = findViewById(R.id.tabMyTeams);
        tabInvitations = findViewById(R.id.tabInvitations);
        rvTeams = findViewById(R.id.rvTeams);
        rvInvitations = findViewById(R.id.rvInvitations);
        tvEmptyTeams = findViewById(R.id.tvEmptyTeams);
        tvEmptyInvitations = findViewById(R.id.tvEmptyInvitations);
        fabCreateTeam = findViewById(R.id.fabCreateTeam);

        btnBack.setOnClickListener(v -> finish());
        
        teamViewModel = new ViewModelProvider(this).get(TeamViewModel.class);
        
        setupTabs();
        
        teamViewModel.teamsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                boolean empty = resource.data.isEmpty();
                tvEmptyTeams.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvTeams.setVisibility(empty ? View.GONE : View.VISIBLE);
                // TODO: Update adapter when TeamAdapter is created
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                tvEmptyTeams.setVisibility(View.VISIBLE);
                rvTeams.setVisibility(View.GONE);
            }
        });
        
        teamViewModel.invitationsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                boolean empty = resource.data.isEmpty();
                tvEmptyInvitations.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvInvitations.setVisibility(empty ? View.GONE : View.VISIBLE);
                // TODO: Update adapter when InvitationAdapter is created
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                tvEmptyInvitations.setVisibility(View.VISIBLE);
                rvInvitations.setVisibility(View.GONE);
            }
        });

        teamViewModel.createTeamState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Tạo đội bóng thành công", Toast.LENGTH_SHORT).show();
                teamViewModel.loadMyTeams();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        fabCreateTeam.setOnClickListener(v -> showCreateTeamDialog());
        
        teamViewModel.loadMyTeams();
        teamViewModel.loadInvitations();
    }

    private void setupTabs() {
        tabMyTeams.setOnClickListener(v -> {
            tabMyTeams.setBackgroundResource(R.drawable.bg_segment_active);
            tabMyTeams.setTextColor(getColor(R.color.text_on_primary));
            tabMyTeams.setTypeface(null, android.graphics.Typeface.BOLD);
            
            tabInvitations.setBackgroundResource(android.R.color.transparent);
            tabInvitations.setTextColor(getColor(R.color.text_secondary));
            tabInvitations.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            rvTeams.setVisibility(View.VISIBLE);
            tvEmptyTeams.setVisibility(View.VISIBLE); // Mock empty state
            
            rvInvitations.setVisibility(View.GONE);
            tvEmptyInvitations.setVisibility(View.GONE);
            fabCreateTeam.setVisibility(View.VISIBLE);
        });
        
        tabInvitations.setOnClickListener(v -> {
            tabInvitations.setBackgroundResource(R.drawable.bg_segment_active);
            tabInvitations.setTextColor(getColor(R.color.text_on_primary));
            tabInvitations.setTypeface(null, android.graphics.Typeface.BOLD);
            
            tabMyTeams.setBackgroundResource(android.R.color.transparent);
            tabMyTeams.setTextColor(getColor(R.color.text_secondary));
            tabMyTeams.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            rvTeams.setVisibility(View.GONE);
            tvEmptyTeams.setVisibility(View.GONE);
            
            rvInvitations.setVisibility(View.GONE);
            tvEmptyInvitations.setVisibility(View.VISIBLE); // Mock empty state
            fabCreateTeam.setVisibility(View.GONE);
        });
    }

    private void showCreateTeamDialog() {
        Dialog dialog = new Dialog(this, R.style.Theme_TimSanBong); // or transparent theme
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_customer_create_team);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.ImageView btnClose = dialog.findViewById(R.id.btnClose);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSave);
        AutoCompleteTextView actvTeamLevel = dialog.findViewById(R.id.actvTeamLevel);

        String[] levels = new String[]{"Mới chơi", "Trung bình", "Khá", "Chuyên nghiệp"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels);
        actvTeamLevel.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String level = actvTeamLevel.getText().toString();
            // Typically we'd have a Team Name input in the dialog too, let's mock the request
            com.example.timsanbong.data.model.TeamRequest req = new com.example.timsanbong.data.model.TeamRequest();
            req.setName("Đội bóng mới");
            req.setSkillLevel(level);
            teamViewModel.createTeam(req);
            dialog.dismiss();
        });

        dialog.show();
    }
}
