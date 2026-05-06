package com.example.timsanbong.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timsanbong.R;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_AUTH = "auth";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_JSON = "user_json";

    private static final DemoAccount[] DEMO_ACCOUNTS = new DemoAccount[] {
            new DemoAccount("player_demo@test.com", "pass123", "Long Travis", "PLAYER", "0901000101", 101),
            new DemoAccount("owner_demo@test.com", "pass123", "Quản lý Sân A", "OWNER", "0902000202", 201),
            new DemoAccount("admin_demo@test.com", "pass123", "Hệ thống Admin", "ADMIN", "0903000303", 301)
    };

    private com.google.android.material.textfield.TextInputLayout tilEmail;
    private com.google.android.material.textfield.TextInputLayout tilPassword;
    private com.google.android.material.button.MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        clearErrors();

        String email = getTextValue(tilEmail);
        String password = getTextValue(tilPassword);

        boolean hasError = false;
        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            hasError = true;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        DemoAccount account = findDemoAccount(email, password);
        if (account == null) {
            tilPassword.setError("Tài khoản demo hoặc mật khẩu không đúng");
            return;
        }

        String token = "DEBUG_TOKEN_" + account.role;
        String userJson = buildUserJson(account);

        getSharedPreferences(PREFS_AUTH, MODE_PRIVATE)
                .edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_JSON, userJson)
                .apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private DemoAccount findDemoAccount(String email, String password) {
        for (DemoAccount account : DEMO_ACCOUNTS) {
            if (account.email.equalsIgnoreCase(email) && account.password.equals(password)) {
                return account;
            }
        }
        return null;
    }

    private String getTextValue(com.google.android.material.textfield.TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) {
            return "";
        }
        return layout.getEditText().getText().toString().trim();
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private String buildUserJson(DemoAccount account) {
        org.json.JSONObject json = new org.json.JSONObject();
        try {
            json.put("id", account.id);
            json.put("fullName", account.fullName);
            json.put("email", account.email);
            json.put("phone", account.phone);
            json.put("role", account.role);
        } catch (org.json.JSONException ignored) {
        }
        return json.toString();
    }

    private static class DemoAccount {
        private final String email;
        private final String password;
        private final String fullName;
        private final String role;
        private final String phone;
        private final long id;

        private DemoAccount(String email, String password, String fullName, String role, String phone, long id) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
            this.phone = phone;
            this.id = id;
        }
    }
}