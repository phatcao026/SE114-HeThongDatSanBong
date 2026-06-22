package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserDetailActivity extends AppCompatActivity {

    private boolean isLocked;
    private long userId;
    private Button btnLockUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        userId = getIntent().getLongExtra("userId", -1);
        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String role = getIntent().getStringExtra("role");
        isLocked = getIntent().getBooleanExtra("isLocked", false);

        // Set UI
        ((TextView) findViewById(R.id.tvFullName)).setText(fullName);
        ((TextView) findViewById(R.id.tvEmail)).setText("Email: " + email);
        ((TextView) findViewById(R.id.tvPhone)).setText("SĐT: " + phone);
        ((TextView) findViewById(R.id.tvRole)).setText(role);
        
        String initials = fullName != null && fullName.length() >= 2 ? 
                fullName.substring(0, 2).toUpperCase() : "U";
        ((TextView) findViewById(R.id.tvInitials)).setText(initials);

        btnLockUser = findViewById(R.id.btnLockUser);
        updateLockButtonStyle();

        btnLockUser.setOnClickListener(v -> {
            if (isLocked) {
                unlockUser();
            } else {
                lockUser();
            }
        });
    }

    private void updateLockButtonStyle() {
        btnLockUser.setTextColor(android.graphics.Color.WHITE);
        if (isLocked) {
            btnLockUser.setText("Mở khóa tài khoản");
            btnLockUser.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.primary)));
        } else {
            btnLockUser.setText("Khóa tài khoản");
            btnLockUser.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.error)));
        }
    }

    private void lockUser() {
        ApiClient.getService(this).lockUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isLocked = true;
                    updateLockButtonStyle();
                    setResult(RESULT_OK);
                    Toast.makeText(AdminUserDetailActivity.this, "Đã khóa tài khoản", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminUserDetailActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlockUser() {
        ApiClient.getService(this).unlockUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isLocked = false;
                    updateLockButtonStyle();
                    setResult(RESULT_OK);
                    Toast.makeText(AdminUserDetailActivity.this, "Đã mở khóa tài khoản", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminUserDetailActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
