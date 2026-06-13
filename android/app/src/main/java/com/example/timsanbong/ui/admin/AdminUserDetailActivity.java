package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.timsanbong.R;

public class AdminUserDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        long userId = getIntent().getLongExtra("userId", -1);
        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String role = getIntent().getStringExtra("role");

        // Set UI
        ((TextView) findViewById(R.id.tvFullName)).setText(fullName);
        ((TextView) findViewById(R.id.tvEmail)).setText("Email: " + email);
        ((TextView) findViewById(R.id.tvPhone)).setText("SĐT: " + phone);
        ((TextView) findViewById(R.id.tvRole)).setText(role);
        
        String initials = fullName != null && fullName.length() >= 2 ? 
                fullName.substring(0, 2).toUpperCase() : "U";
        ((TextView) findViewById(R.id.tvInitials)).setText(initials);

        findViewById(R.id.btnLockUser).setOnClickListener(v -> {
            Toast.makeText(this, "Đã khóa người dùng " + fullName, Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.btnResetPassword).setOnClickListener(v -> {
            Toast.makeText(this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
        });
    }
}
