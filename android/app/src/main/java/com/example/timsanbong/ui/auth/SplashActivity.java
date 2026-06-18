package com.example.timsanbong.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.User;
import com.example.timsanbong.ui.admin.AdminMainActivity;
import com.example.timsanbong.ui.customer.MainActivity;
import com.example.timsanbong.ui.owner.OwnerDashboardActivity;
import com.example.timsanbong.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_splash);

        sessionManager = new SessionManager(this);
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuth, SPLASH_DELAY);
    }

    private void checkAuth() {
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        ApiClient.getService(this).getMyProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUser(response.body());
                    navigateByRole();
                } else {
                    clearSessionAndLogin();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                clearSessionAndLogin();
            }
        });
    }

    private void saveUser(User user) {
        sessionManager.saveUserId(user.getId());
        sessionManager.saveUserRole(user.getRole());
        sessionManager.saveUserEmail(user.getEmail());

        try {
            JSONObject userJson = new JSONObject();
            userJson.put("id", user.getId());
            userJson.put("fullName", user.getFullName());
            userJson.put("email", user.getEmail());
            userJson.put("phone", user.getPhone());
            userJson.put("role", user.getRole());
            sessionManager.saveUserJson(userJson.toString());
        } catch (JSONException ignored) {
        }
    }

    private void navigateByRole() {
        Class<?> destination = MainActivity.class;
        if (sessionManager.isOwner()) {
            destination = OwnerDashboardActivity.class;
        } else if (sessionManager.isAdmin()) {
            destination = AdminMainActivity.class;
        }

        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void clearSessionAndLogin() {
        sessionManager.clearSession();
        ApiClient.reset();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
