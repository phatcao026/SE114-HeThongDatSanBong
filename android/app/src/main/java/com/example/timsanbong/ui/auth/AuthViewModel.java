package com.example.timsanbong.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.AuthResponse;
import com.example.timsanbong.data.repository.AuthRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private final SessionManager sessionManager;

    private final MutableLiveData<Resource<AuthResponse>> _registerState = new MutableLiveData<>();
    public LiveData<Resource<AuthResponse>> registerState = _registerState;

    private final MutableLiveData<String> _loginMessage = new MutableLiveData<>();
    public LiveData<String> loginMessage = _loginMessage;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application);
    }

    public boolean loginDemo(String email, String password) {
        DemoAccount account = findDemoAccount(email, password);
        if (account == null) {
            _loginMessage.setValue("Tài khoản demo hoặc mật khẩu không đúng");
            return false;
        }

        String token = "DEBUG_TOKEN_" + account.role;
        String userJson = buildUserJson(account);

        sessionManager.saveToken(token);
        sessionManager.saveUserJson(userJson);

        return true;
    }

    public void register(String fullName, String email, String password) {
        _registerState.setValue(Resource.loading(null));

        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("password", password);

        authRepository.register(getApplication(), body, new RepositoryCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                if (data.getToken() != null && data.getUser() != null) {
                    sessionManager.saveToken(data.getToken());
                    try {
                        JSONObject userJson = new JSONObject();
                        userJson.put("id", data.getUser().getId());
                        userJson.put("fullName", data.getUser().getFullName());
                        userJson.put("email", data.getUser().getEmail());
                        userJson.put("phone", data.getUser().getPhone());
                        userJson.put("role", data.getUser().getRole());
                        sessionManager.saveUserJson(userJson.toString());
                    } catch (JSONException ignored) {
                    }
                }
                _registerState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _registerState.postValue(Resource.error(message, null));
            }
        });
    }

    private DemoAccount findDemoAccount(String email, String password) {
        DemoAccount[] demoAccounts = new DemoAccount[]{
                new DemoAccount("player_demo@test.com", "pass123", "Long Travis", "PLAYER", "0901000101", 101),
                new DemoAccount("owner_demo@test.com", "pass123", "Quản lý Sân A", "OWNER", "0902000202", 201),
                new DemoAccount("admin_demo@test.com", "pass123", "Hệ thống Admin", "ADMIN", "0903000303", 301)
        };

        for (DemoAccount account : demoAccounts) {
            if (account.email.equalsIgnoreCase(email) && account.password.equals(password)) {
                return account;
            }
        }
        return null;
    }

    private String buildUserJson(DemoAccount account) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", account.id);
            json.put("fullName", account.fullName);
            json.put("email", account.email);
            json.put("phone", account.phone);
            json.put("role", account.role);
            return json.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    private static class DemoAccount {
        String email, password, fullName, role, phone;
        long id;

        DemoAccount(String email, String password, String fullName, String role, String phone, long id) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
            this.phone = phone;
            this.id = id;
        }
    }
}
