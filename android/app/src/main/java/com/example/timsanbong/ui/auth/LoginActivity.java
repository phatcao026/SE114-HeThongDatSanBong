package com.example.timsanbong.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.admin.AdminMainActivity;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.owner.OwnerDashboardActivity;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private MaterialButton btnLogin;
    private AuthViewModel authViewModel;
    private int debugTapCount = 0;
    private static final int DEBUG_TAP_THRESHOLD = 3;
    private static final long DEBUG_TAP_TIMEOUT = 1500; // 1.5 seconds
    private Handler debugHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        // Debug: 3-tap bypass on title
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(v -> handleDebugTap());

        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        authViewModel.loginMessage.observe(this, message -> {
            tilPassword.setError(message);
        });
        authViewModel.loginSuccess.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                navigateByRole();
            }
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

        if (Constants.MOCK_MODE) {
            if (authViewModel.loginDemo(email, password)) {
                navigateByRole();
            }
        } else {
            authViewModel.login(email, password);
        }
    }

    private String getTextValue(TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) {
            return "";
        }
        return layout.getEditText().getText().toString().trim();
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void handleDebugTap() {
        debugTapCount++;
        debugHandler.removeCallbacksAndMessages(null);

        if (debugTapCount == DEBUG_TAP_THRESHOLD) {
            bypassLogin();
            debugTapCount = 0;
        } else {
            // Reset counter after timeout
            debugHandler.postDelayed(() -> debugTapCount = 0, DEBUG_TAP_TIMEOUT);
        }
    }

    private void bypassLogin() {
        // Auto-login with demo account
        if (authViewModel.loginDemo("player_demo@test.com", "pass123")) {
            navigateByRole();
        }
    }

    private void navigateByRole() {
        SessionManager sessionManager = new SessionManager(this);
        Class<?> destination = MainActivity.class;

        try {
            JSONObject userJson = new JSONObject(sessionManager.getUserJson());
            String role = userJson.optString("role", "PLAYER");
            if ("OWNER".equalsIgnoreCase(role)) {
                destination = OwnerDashboardActivity.class;
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                destination = AdminMainActivity.class;
            }
        } catch (JSONException ignored) {
        }

        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
