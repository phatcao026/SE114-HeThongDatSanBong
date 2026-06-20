package com.example.timsanbong.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
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

public class RegisterActivity extends AppCompatActivity {

    private static final int RESEND_COOLDOWN_SECONDS = 30;

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputLayout tilRegisterOtp;
    private MaterialButton btnSendRegisterOtp;
    private MaterialButton btnRegister;
    private MaterialButton btnResendRegisterOtp;
    private ViewFlipper registerViewFlipper;
    private AuthViewModel authViewModel;
    private int resendSecondsRemaining = 0;
    private final Handler resendHandler = new Handler(Looper.getMainLooper());
    private final Runnable resendTick = new Runnable() {
        @Override
        public void run() {
            if (resendSecondsRemaining > 0) {
                resendSecondsRemaining--;
                updateResendButton();
                resendHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        registerViewFlipper = findViewById(R.id.registerViewFlipper);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilRegisterOtp = findViewById(R.id.tilRegisterOtp);
        btnSendRegisterOtp = findViewById(R.id.btnSendRegisterOtp);
        btnRegister = findViewById(R.id.btnRegister);
        btnResendRegisterOtp = findViewById(R.id.btnResendRegisterOtp);

        btnSendRegisterOtp.setOnClickListener(v -> requestRegisterOtp());
        btnResendRegisterOtp.setOnClickListener(v -> requestRegisterOtp());
        btnRegister.setOnClickListener(v -> attemptRegister());

        findViewById(R.id.btnBackFromRegisterOtp).setOnClickListener(v -> showRegisterStep(0, false));

        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> finish());

        observeViewModel();
        updateResendButton();
    }

    private void observeViewModel() {
        authViewModel.registerOtpState.observe(this, resource -> {
            if (resource == null) return;

            boolean loading = resource.status == Resource.Status.LOADING;
            btnSendRegisterOtp.setEnabled(!loading);
            if (resendSecondsRemaining == 0) {
                btnResendRegisterOtp.setEnabled(!loading);
            }

            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Đã gửi OTP. Vui lòng kiểm tra email.", Toast.LENGTH_SHORT).show();
                showRegisterStep(1, true);
                startResendCooldown();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                tilEmail.setError(resource.message);
            }
        });

        authViewModel.registerState.observe(this, resource -> {
            if (resource == null) return;

            btnRegister.setEnabled(resource.status != Resource.Status.LOADING);
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Đăng ký thành công.", Toast.LENGTH_SHORT).show();
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                tilRegisterOtp.setError(resource.message);
            }
        });
    }

    private void requestRegisterOtp() {
        if (!validateRegistrationForm()) {
            return;
        }

        authViewModel.sendRegisterOtp(getTextValue(tilEmail));
    }

    private void attemptRegister() {
        clearErrors();

        if (!validateRegistrationForm()) {
            return;
        }

        String otp = getTextValue(tilRegisterOtp);
        if (otp.isEmpty()) {
            tilRegisterOtp.setError("Vui lòng nhập OTP");
            return;
        }

        authViewModel.register(
                getTextValue(tilName),
                getTextValue(tilEmail),
                getTextValue(tilPassword),
                otp
        );
    }

    private boolean validateRegistrationForm() {
        clearErrors();

        String fullName = getTextValue(tilName);
        String email = getTextValue(tilEmail);
        String password = getTextValue(tilPassword);
        String confirmPassword = getTextValue(tilConfirmPassword);

        boolean hasError = false;
        if (fullName.isEmpty()) {
            tilName.setError("Vui lòng nhập họ tên");
            hasError = true;
        }

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
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            hasError = true;
        }

        return !hasError;
    }

    private void showRegisterStep(int child, boolean forward) {
        registerViewFlipper.setInAnimation(this, forward ? R.anim.slide_in_right : R.anim.slide_in_left);
        registerViewFlipper.setOutAnimation(this, forward ? R.anim.slide_out_left : R.anim.slide_out_right);
        registerViewFlipper.setDisplayedChild(child);
    }

    private void startResendCooldown() {
        resendSecondsRemaining = RESEND_COOLDOWN_SECONDS;
        resendHandler.removeCallbacks(resendTick);
        updateResendButton();
        resendHandler.postDelayed(resendTick, 1000);
    }

    private void updateResendButton() {
        if (btnResendRegisterOtp == null) {
            return;
        }

        if (resendSecondsRemaining > 0) {
            btnResendRegisterOtp.setEnabled(false);
            btnResendRegisterOtp.setText("Gửi lại OTP (" + resendSecondsRemaining + "s)");
        } else {
            btnResendRegisterOtp.setEnabled(true);
            btnResendRegisterOtp.setText("Gửi lại OTP");
        }
    }

    private String getTextValue(TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) {
            return "";
        }
        return layout.getEditText().getText().toString().trim();
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilRegisterOtp.setError(null);
    }

    @Override
    public void onBackPressed() {
        if (registerViewFlipper != null && registerViewFlipper.getDisplayedChild() == 1) {
            showRegisterStep(0, false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        resendHandler.removeCallbacks(resendTick);
        super.onDestroy();
    }
}
