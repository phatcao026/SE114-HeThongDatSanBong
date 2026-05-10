package com.example.timsanbong.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.customer.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private MaterialButton btnLogin;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        authViewModel.loginMessage.observe(this, message -> {
            tilPassword.setError(message);
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

        if (authViewModel.loginDemo(email, password)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
}
