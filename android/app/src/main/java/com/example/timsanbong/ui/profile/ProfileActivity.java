package com.example.timsanbong.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.auth.LoginActivity;
import com.example.timsanbong.ui.customer.MyBookingsActivity;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone;
    private MaterialButton btnMyBookings, btnLogout;
    private NavBarManager navBarManager;
    private ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        btnMyBookings = findViewById(R.id.btnMyBookings);
        btnLogout = findViewById(R.id.btnLogout);

        btnMyBookings.setOnClickListener(v -> startActivity(new Intent(this, MyBookingsActivity.class)));
        btnLogout.setOnClickListener(v -> profileViewModel.logout());

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_PROFILE);
        navBarManager.setup();

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.profileState.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                // Show loading if desired
            } else if (resource.status == Resource.Status.SUCCESS) {
                tvName.setText(resource.data.getFullName());
                tvEmail.setText(resource.data.getEmail());
                tvPhone.setText(resource.data.getPhone());
            } else {
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
}
