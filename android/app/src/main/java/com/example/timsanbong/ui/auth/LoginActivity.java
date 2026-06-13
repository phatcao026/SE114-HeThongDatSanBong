package com.example.timsanbong.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.admin.AdminMainActivity;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.owner.OwnerDashboardActivity;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private MaterialButton btnLogin;
    private android.widget.ViewFlipper viewFlipper;
    private AuthViewModel authViewModel;
    private int debugTapCount = 0;
    private static final int DEBUG_TAP_THRESHOLD = 3;
    private static final long DEBUG_TAP_TIMEOUT = 1500;
    private final Handler debugHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        viewFlipper = findViewById(R.id.viewFlipper);

        btnLogin.setOnClickListener(v -> attemptLogin());

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(v -> handleDebugTap());

        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        authViewModel.loginMessage.observe(this, message -> tilPassword.setError(message));

        authViewModel.registerState.observe(this, state -> {
            if (state == null) return;
            btnLogin.setEnabled(state.status != Resource.Status.LOADING);
        });

        authViewModel.loginSuccess.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                navigateByRole();
            }
        });

        setupGoogleLogin();
        setupForgotPasswordFlow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith("timsanbong://auth")) {
            String token = uri.getQueryParameter("token");
            if (token != null) {
                authViewModel.googleSync(token);
                getIntent().setData(null);
            }
        }
    }

    private void setupGoogleLogin() {
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> authViewModel.getGoogleUrl());

        authViewModel.googleUrlState.observe(this, state -> {
            if (state == null) return;
            if (state.status == Resource.Status.SUCCESS && state.data != null) {
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                customTabsIntent.launchUrl(this, Uri.parse(state.data));
            } else if (state.status == Resource.Status.ERROR) {
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupForgotPasswordFlow() {
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        android.widget.ImageView btnBackFromForgot = findViewById(R.id.btnBackFromForgot);
        android.widget.ImageView btnBackFromOtp = findViewById(R.id.btnBackFromOtp);
        android.widget.ImageView btnBackFromReset = findViewById(R.id.btnBackFromReset);
        MaterialButton btnSendOtp = findViewById(R.id.btnSendOtp);
        MaterialButton btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        MaterialButton btnResetPassword = findViewById(R.id.btnResetPassword);

        tvForgotPassword.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.setDisplayedChild(1);
        });

        btnBackFromForgot.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(0);
        });

        btnSendOtp.setOnClickListener(v -> {
            String email = getTextValue(tilEmail);
            if (email.isEmpty()) {
                tilEmail.setError("Vui lòng nhập email");
                return;
            }
            authViewModel.forgotPassword(email);
        });

        authViewModel.forgotPasswordState.observe(this, state -> {
            if (state == null) return;
            switch (state.status) {
                case SUCCESS:
                    if (viewFlipper.getDisplayedChild() == 1) {
                        Toast.makeText(this, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show();
                        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
                        viewFlipper.setDisplayedChild(2);
                    } else if (viewFlipper.getDisplayedChild() == 2) {
                        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
                        viewFlipper.setDisplayedChild(3);
                    } else if (viewFlipper.getDisplayedChild() == 3) {
                        Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        viewFlipper.setInAnimation(this, R.anim.slide_in_left);
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
                        viewFlipper.setDisplayedChild(0);
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });

        btnBackFromOtp.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(1);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String email = getTextValue(tilEmail);
            authViewModel.verifyOtp(email, "123456");
        });

        btnBackFromReset.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(2);
        });

        btnResetPassword.setOnClickListener(v -> {
            String email = getTextValue(tilEmail);
            authViewModel.resetPassword(email, "123456", "newpassword123");
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

        if (!hasError) {
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
            debugHandler.postDelayed(() -> debugTapCount = 0, DEBUG_TAP_TIMEOUT);
        }
    }

    private void bypassLogin() {
        Toast.makeText(this, "Debug login is disabled. Use a real backend account.", Toast.LENGTH_SHORT).show();
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
