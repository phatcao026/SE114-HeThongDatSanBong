package com.example.timsanbong.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.admin.AdminMainActivity;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.owner.OwnerMainActivity;
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

        setupForgotPasswordFlow();
    }

    private void setupForgotPasswordFlow() {
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextInputLayout tilForgotEmail = findViewById(R.id.tilForgotEmail);
        TextInputLayout tilOtp = findViewById(R.id.tilOtp);
        TextInputLayout tilNewPassword = findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        android.widget.ImageView btnBackFromForgot = findViewById(R.id.btnBackFromForgot);
        android.widget.ImageView btnBackFromOtp = findViewById(R.id.btnBackFromOtp);
        android.widget.ImageView btnBackFromReset = findViewById(R.id.btnBackFromReset);
        MaterialButton btnSendOtp = findViewById(R.id.btnSendOtp);
        MaterialButton btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        MaterialButton btnResetPassword = findViewById(R.id.btnResetPassword);

        tvForgotPassword.setOnClickListener(v -> {
            clearForgotErrors(tilForgotEmail, tilOtp, tilNewPassword, tilConfirmPassword);
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.setDisplayedChild(1);
        });

        btnBackFromForgot.setOnClickListener(v -> showFlipperChild(0, false));
        btnBackFromOtp.setOnClickListener(v -> showFlipperChild(1, false));
        btnBackFromReset.setOnClickListener(v -> showFlipperChild(2, false));

        btnSendOtp.setOnClickListener(v -> {
            clearForgotErrors(tilForgotEmail, tilOtp, tilNewPassword, tilConfirmPassword);
            String email = getTextValue(tilForgotEmail);
            if (email.isEmpty()) {
                tilForgotEmail.setError("Vui long nhap email");
                return;
            }
            authViewModel.forgotPassword(email);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            clearForgotErrors(tilForgotEmail, tilOtp, tilNewPassword, tilConfirmPassword);
            String email = getTextValue(tilForgotEmail);
            String otp = getTextValue(tilOtp);
            if (email.isEmpty()) {
                tilForgotEmail.setError("Vui long nhap email");
                return;
            }
            if (otp.isEmpty()) {
                tilOtp.setError("Vui long nhap OTP");
                return;
            }
            authViewModel.verifyOtp(email, otp);
        });

        btnResetPassword.setOnClickListener(v -> {
            clearForgotErrors(tilForgotEmail, tilOtp, tilNewPassword, tilConfirmPassword);
            String email = getTextValue(tilForgotEmail);
            String otp = getTextValue(tilOtp);
            String newPassword = getTextValue(tilNewPassword);
            String confirmPassword = getTextValue(tilConfirmPassword);
            if (newPassword.isEmpty()) {
                tilNewPassword.setError("Vui long nhap mat khau moi");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Mat khau khong khop");
                return;
            }
            authViewModel.resetPassword(email, otp, newPassword);
        });

        authViewModel.forgotPasswordState.observe(this, state -> {
            if (state == null) return;
            switch (state.status) {
                case SUCCESS:
                    if (viewFlipper.getDisplayedChild() == 1) {
                        Toast.makeText(this, "Da gui ma OTP", Toast.LENGTH_SHORT).show();
                        showFlipperChild(2, true);
                    } else if (viewFlipper.getDisplayedChild() == 2) {
                        showFlipperChild(3, true);
                    } else if (viewFlipper.getDisplayedChild() == 3) {
                        Toast.makeText(this, "Dat lai mat khau thanh cong", Toast.LENGTH_SHORT).show();
                        showFlipperChild(0, false);
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void showFlipperChild(int child, boolean forward) {
        viewFlipper.setInAnimation(this, forward ? R.anim.slide_in_right : R.anim.slide_in_left);
        viewFlipper.setOutAnimation(this, forward ? R.anim.slide_out_left : R.anim.slide_out_right);
        viewFlipper.setDisplayedChild(child);
    }

    private void clearForgotErrors(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            layout.setError(null);
        }
    }

    private void attemptLogin() {
        clearErrors();

        String email = getTextValue(tilEmail);
        String password = getTextValue(tilPassword);

        boolean hasError = false;
        if (email.isEmpty()) {
            tilEmail.setError("Vui long nhap email");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email khong hop le");
            hasError = true;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Vui long nhap mat khau");
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
                destination = OwnerMainActivity.class;
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
