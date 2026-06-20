package com.example.timsanbong.ui.customer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.InvitationResponse;
import com.example.timsanbong.data.model.TeamRequest;
import com.example.timsanbong.data.model.TeamResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TeamManagementActivity extends AppCompatActivity {

    private android.widget.ImageView btnBack;
    private TextView tabMyTeams, tabInvitations;
    private RecyclerView rvTeams, rvInvitations;
    private TextView tvEmptyTeams, tvEmptyInvitations;
    private FloatingActionButton fabCreateTeam;
    private TeamViewModel teamViewModel;
    private TeamAdapter teamAdapter;
    private InvitationAdapter invitationAdapter;
    private boolean showingInvitations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_team_management);
        initViews();
        setupTabs();
        setupObservers();

        btnBack.setOnClickListener(v -> finish());
        fabCreateTeam.setOnClickListener(v -> showCreateTeamDialog());

        teamViewModel.loadMyTeams();
        teamViewModel.loadInvitations();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabMyTeams = findViewById(R.id.tabMyTeams);
        tabInvitations = findViewById(R.id.tabInvitations);
        rvTeams = findViewById(R.id.rvTeams);
        rvInvitations = findViewById(R.id.rvInvitations);
        tvEmptyTeams = findViewById(R.id.tvEmptyTeams);
        tvEmptyInvitations = findViewById(R.id.tvEmptyInvitations);
        fabCreateTeam = findViewById(R.id.fabCreateTeam);

        teamViewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        teamAdapter = new TeamAdapter();
        invitationAdapter = new InvitationAdapter();
        rvTeams.setLayoutManager(new LinearLayoutManager(this));
        rvTeams.setAdapter(teamAdapter);
        rvInvitations.setLayoutManager(new LinearLayoutManager(this));
        rvInvitations.setAdapter(invitationAdapter);
    }

    private void setupTabs() {
        tabMyTeams.setOnClickListener(v -> {
            showingInvitations = false;
            updateTabs();
        });

        tabInvitations.setOnClickListener(v -> {
            showingInvitations = true;
            updateTabs();
        });

        updateTabs();
    }

    private void setupObservers() {
        teamViewModel.teamsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                teamAdapter.updateTeams(resource.data);
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                teamAdapter.updateTeams(new ArrayList<>());
            }
            updateTabs();
        });

        teamViewModel.invitationsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                invitationAdapter.updateInvitations(resource.data);
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                invitationAdapter.updateInvitations(new ArrayList<>());
            }
            updateTabs();
        });

        teamViewModel.createTeamState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Tao doi bong thanh cong.", Toast.LENGTH_SHORT).show();
                teamViewModel.loadMyTeams();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTabs() {
        tabMyTeams.setBackgroundResource(!showingInvitations ? R.drawable.bg_segment_active : android.R.color.transparent);
        tabMyTeams.setTextColor(getColor(!showingInvitations ? R.color.text_on_primary : R.color.text_secondary));
        tabMyTeams.setTypeface(null, !showingInvitations ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        tabInvitations.setBackgroundResource(showingInvitations ? R.drawable.bg_segment_active : android.R.color.transparent);
        tabInvitations.setTextColor(getColor(showingInvitations ? R.color.text_on_primary : R.color.text_secondary));
        tabInvitations.setTypeface(null, showingInvitations ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        boolean teamsEmpty = teamAdapter.getItemCount() == 0;
        boolean invitationsEmpty = invitationAdapter.getItemCount() == 0;

        rvTeams.setVisibility(!showingInvitations && !teamsEmpty ? View.VISIBLE : View.GONE);
        tvEmptyTeams.setVisibility(!showingInvitations && teamsEmpty ? View.VISIBLE : View.GONE);
        rvInvitations.setVisibility(showingInvitations && !invitationsEmpty ? View.VISIBLE : View.GONE);
        tvEmptyInvitations.setVisibility(showingInvitations && invitationsEmpty ? View.VISIBLE : View.GONE);
        fabCreateTeam.setVisibility(showingInvitations ? View.GONE : View.VISIBLE);
    }

    private void showCreateTeamDialog() {
        Dialog dialog = new Dialog(this, R.style.Theme_TimSanBong);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_customer_create_team);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.ImageView btnClose = dialog.findViewById(R.id.btnClose);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSave);
        TextInputEditText etTeamName = dialog.findViewById(R.id.etTeamName);
        TextInputEditText etTeamDescription = dialog.findViewById(R.id.etTeamDescription);
        AutoCompleteTextView actvTeamLevel = dialog.findViewById(R.id.actvTeamLevel);

        String[] levels = new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels);
        actvTeamLevel.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etTeamName.getText() != null ? etTeamName.getText().toString().trim() : "";
            String level = actvTeamLevel.getText().toString().trim();
            String description = etTeamDescription.getText() != null ? etTeamDescription.getText().toString().trim() : "";
            if (name.isEmpty() || level.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            TeamRequest request = new TeamRequest();
            request.setName(name);
            request.setSkillLevel(level);
            request.setDescription(description);
            teamViewModel.createTeam(request);
            dialog.dismiss();
        });

        dialog.show();
    }

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "FC";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private static class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {
        private final List<TeamResponse> teams = new ArrayList<>();

        void updateTeams(List<TeamResponse> newTeams) {
            teams.clear();
            teams.addAll(newTeams);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_team, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TeamResponse team = teams.get(position);
            holder.tvTeamInitials.setText(initials(team.getName()));
            holder.tvTeamName.setText(team.getName());
            holder.tvTeamLevel.setText(team.getLevel());
            holder.tvMemberCount.setText(String.valueOf(team.getMemberCount()));
            holder.tvTeamDescription.setText(team.getDescription());
            holder.tvTeamDescription.setVisibility(team.getDescription() == null || team.getDescription().isEmpty()
                    ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return teams.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvTeamInitials;
            final TextView tvTeamName;
            final TextView tvTeamLevel;
            final TextView tvMemberCount;
            final TextView tvTeamDescription;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTeamInitials = itemView.findViewById(R.id.tvTeamInitials);
                tvTeamName = itemView.findViewById(R.id.tvTeamName);
                tvTeamLevel = itemView.findViewById(R.id.tvTeamLevel);
                tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
                tvTeamDescription = itemView.findViewById(R.id.tvTeamDescription);
            }
        }
    }

    private static class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {
        private final List<InvitationResponse> invitations = new ArrayList<>();

        void updateInvitations(List<InvitationResponse> newInvitations) {
            invitations.clear();
            invitations.addAll(newInvitations);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_team, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            InvitationResponse invitation = invitations.get(position);
            holder.tvTeamInitials.setText(initials(invitation.getTeam()));
            holder.tvTeamName.setText(invitation.getTeam());
            holder.tvTeamLevel.setText(invitation.getStatus());
            holder.tvMemberCount.setText("");
            holder.tvTeamDescription.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return invitations.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvTeamInitials;
            final TextView tvTeamName;
            final TextView tvTeamLevel;
            final TextView tvMemberCount;
            final TextView tvTeamDescription;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTeamInitials = itemView.findViewById(R.id.tvTeamInitials);
                tvTeamName = itemView.findViewById(R.id.tvTeamName);
                tvTeamLevel = itemView.findViewById(R.id.tvTeamLevel);
                tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
                tvTeamDescription = itemView.findViewById(R.id.tvTeamDescription);
            }
        }
    }
}
