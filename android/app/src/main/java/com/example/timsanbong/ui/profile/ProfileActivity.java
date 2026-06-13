package com.example.timsanbong.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.auth.LoginActivity;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvTrustScore;
    private android.widget.ProgressBar pbTrustScore;
    private TextView tabPersonalInfo, tabBookingHistory;
    private android.view.View cardPersonalInfo, layoutBookingHistory;
    private MaterialButton btnLogout;
    private ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        pbTrustScore = findViewById(R.id.pbTrustScore);
        tabPersonalInfo = findViewById(R.id.tabPersonalInfo);
        tabBookingHistory = findViewById(R.id.tabBookingHistory);
        cardPersonalInfo = findViewById(R.id.cardPersonalInfo);
        layoutBookingHistory = findViewById(R.id.layoutBookingHistory);
        btnLogout = findViewById(R.id.btnLogout);
        android.widget.ImageView btnSettings = findViewById(R.id.btnSettings);

        btnLogout.setOnClickListener(v -> profileViewModel.logout());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.timsanbong.ui.customer.TeamManagementActivity.class)));

        setupTabs();
        animateTrustScore(92);

        new NavBarManager(this, NavBarManager.ITEM_HOME).setup();

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.profileState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                tvName.setText(resource.data.getFullName());
                tvEmail.setText(resource.data.getEmail());
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.logoutEvent.observe(this, shouldLogout -> {
            if (shouldLogout) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        profileViewModel.loadProfile();
    }

    private void setupTabs() {
        tabPersonalInfo.setOnClickListener(v -> {
            tabPersonalInfo.setBackgroundResource(R.drawable.bg_segment_active);
            tabPersonalInfo.setTextColor(getColor(R.color.text_on_primary));
            tabPersonalInfo.setTypeface(null, android.graphics.Typeface.BOLD);

            tabBookingHistory.setBackgroundResource(android.R.color.transparent);
            tabBookingHistory.setTextColor(getColor(R.color.text_secondary));
            tabBookingHistory.setTypeface(null, android.graphics.Typeface.NORMAL);

            cardPersonalInfo.setVisibility(android.view.View.VISIBLE);
            layoutBookingHistory.setVisibility(android.view.View.GONE);
        });

        tabBookingHistory.setOnClickListener(v -> {
            tabBookingHistory.setBackgroundResource(R.drawable.bg_segment_active);
            tabBookingHistory.setTextColor(getColor(R.color.text_on_primary));
            tabBookingHistory.setTypeface(null, android.graphics.Typeface.BOLD);

            tabPersonalInfo.setBackgroundResource(android.R.color.transparent);
            tabPersonalInfo.setTextColor(getColor(R.color.text_secondary));
            tabPersonalInfo.setTypeface(null, android.graphics.Typeface.NORMAL);

            cardPersonalInfo.setVisibility(android.view.View.GONE);
            layoutBookingHistory.setVisibility(android.view.View.VISIBLE);
        });
    }

    private void animateTrustScore(int score) {
        tvTrustScore.setText(score + "/100");
        android.animation.ObjectAnimator animation =
                android.animation.ObjectAnimator.ofInt(pbTrustScore, "progress", 0, score);
        animation.setDuration(1000);
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animation.start();
    }
}
