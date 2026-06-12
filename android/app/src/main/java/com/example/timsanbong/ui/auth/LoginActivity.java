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
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private MaterialButton btnLogin;
    private android.widget.ViewFlipper viewFlipper;
    private AuthViewModel authViewModel;
    private int debugTapCount = 0;
    private static final int DEBUG_TAP_THRESHOLD = 3;
    private static final long DEBUG_TAP_TIMEOUT = 1500; // 1.5 seconds
    private Handler debugHandler = new Handler(Looper.getMainLooper());

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

        // Google Login
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> authViewModel.getGoogleUrl());
        
        authViewModel.googleUrlState.observe(this, state -> {
            if (state == null) return;
            if (state.status == Resource.Status.SUCCESS && state.data != null) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(this, Uri.parse(state.data));
            } else if (state.status == Resource.Status.ERROR) {
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
            }
        });

        setupForgotPasswordFlow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if returning from Google Auth
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith("timsanbong://auth")) {
            String token = uri.getQueryParameter("token");
            if (token != null) {
                authViewModel.googleSync(token);
                // Clear the intent data so we don't process it again
                getIntent().setData(null);
            }
        }
    }

    private void setupForgotPasswordFlow() {
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        android.widget.ImageView btnBackFromForgot = findViewById(R.id.btnBackFromForgot);
        android.widget.ImageView btnBackFromOtp = findViewById(R.id.btnBackFromOtp);
        android.widget.ImageView btnBackFromReset = findViewById(R.id.btnBackFromReset);
        MaterialButton btnSendOtp = findViewById(R.id.btnSendOtp);
        MaterialButton btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        MaterialButton btnResetPassword = findViewById(R.id.btnResetPassword);

        // From Login -> Enter Email
        tvForgotPassword.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.setDisplayedChild(1);
        });

        // From Enter Email -> Login
        btnBackFromForgot.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(0);
        });

        // From Enter Email -> Enter OTP
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
                case LOADING:
                    // show loading
                    break;
                case SUCCESS:
                    // If we just sent OTP
                    if (viewFlipper.getDisplayedChild() == 1) {
                        android.widget.Toast.makeText(this, "Đã gửi mã OTP", android.widget.Toast.LENGTH_SHORT).show();
                        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
                        viewFlipper.setDisplayedChild(2);
                    } 
                    // If we just verified OTP
                    else if (viewFlipper.getDisplayedChild() == 2) {
                        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
                        viewFlipper.setDisplayedChild(3);
                    }
                    // If we just reset password
                    else if (viewFlipper.getDisplayedChild() == 3) {
                        android.widget.Toast.makeText(this, "Đặt lại mật khẩu thành công!", android.widget.Toast.LENGTH_SHORT).show();
                        viewFlipper.setInAnimation(this, R.anim.slide_in_left);
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
                        viewFlipper.setDisplayedChild(0);
                    }
                    break;
                case ERROR:
                    android.widget.Toast.makeText(this, state.message, android.widget.Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // From Enter OTP -> Enter Email
        btnBackFromOtp.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(1);
        });

        // From Enter OTP -> Reset Password
        btnVerifyOtp.setOnClickListener(v -> {
            // Need OTP input field (assuming there is one or mocked here for demo)
            String email = getTextValue(tilEmail);
            authViewModel.verifyOtp(email, "123456"); // Mocking OTP input from UI for now
        });

        // From Reset Password -> Enter OTP
        btnBackFromReset.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(2);
        });

        // Complete Reset
        btnResetPassword.setOnClickListener(v -> {
            String email = getTextValue(tilEmail);
            authViewModel.resetPassword(email, "123456", "newpassword123"); // Mocking new password from UI for now
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

        authViewModel.login(email, password);
        
        authViewModel.registerState.observe(this, state -> {
            if (state == null) return;
            switch (state.status) {
                case LOADING:
                    btnLogin.setEnabled(false);
                    break;
                case SUCCESS:
                    btnLogin.setEnabled(true);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                case ERROR:
                    btnLogin.setEnabled(true);
                    // Error message handled by loginMessage observer
                    break;
            }
        });
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
        // Auto-login with real API if desired, or skip.
        // authViewModel.login("player@example.com", "password");
    }
}
