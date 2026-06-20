package com.example.timsanbong.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AuthResponse;
import com.example.timsanbong.data.model.User;
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

    private final MutableLiveData<Resource<Void>> _registerOtpState = new MutableLiveData<>();
    public LiveData<Resource<Void>> registerOtpState = _registerOtpState;

    private final MutableLiveData<String> _loginMessage = new MutableLiveData<>();
    public LiveData<String> loginMessage = _loginMessage;

    private final MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>();
    public LiveData<Boolean> loginSuccess = _loginSuccess;

    private final MutableLiveData<Resource<Void>> _forgotPasswordState = new MutableLiveData<>();
    public LiveData<Resource<Void>> forgotPasswordState = _forgotPasswordState;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application);
    }

    public void login(String email, String password) {
        _registerState.setValue(Resource.loading(null));

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        authRepository.login(getApplication(), body, new RepositoryCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                saveToken(data);
                loadProfileAfterAuth();
            }

            @Override
            public void onError(String message) {
                _loginMessage.postValue(message);
                _registerState.postValue(Resource.error(message, null));
                _loginSuccess.postValue(false);
            }
        });
    }

    public void sendRegisterOtp(String email) {
        _registerOtpState.setValue(Resource.loading(null));

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        authRepository.sendRegisterOtp(getApplication(), body, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _registerOtpState.postValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                _registerOtpState.postValue(Resource.error(message, null));
            }
        });
    }

    public void register(String fullName, String email, String password, String otp) {
        _registerState.setValue(Resource.loading(null));

        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("email", email);
        body.put("password", password);
        body.put("otp", otp);

        authRepository.register(getApplication(), body, new RepositoryCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                _registerState.postValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                _registerState.postValue(Resource.error(message, null));
            }
        });
    }

    public void forgotPassword(String email) {
        _forgotPasswordState.setValue(Resource.loading(null));
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        authRepository.forgotPassword(getApplication(), body, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) { _forgotPasswordState.postValue(Resource.success(null)); }

            @Override
            public void onError(String msg) { _forgotPasswordState.postValue(Resource.error(msg, null)); }
        });
    }

    public void verifyOtp(String email, String otp) {
        _forgotPasswordState.setValue(Resource.loading(null));
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);
        authRepository.verifyOtp(getApplication(), body, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) { _forgotPasswordState.postValue(Resource.success(null)); }

            @Override
            public void onError(String msg) { _forgotPasswordState.postValue(Resource.error(msg, null)); }
        });
    }

    public void resetPassword(String email, String otp, String newPassword) {
        _forgotPasswordState.setValue(Resource.loading(null));
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);
        body.put("newPassword", newPassword);
        authRepository.resetPassword(getApplication(), body, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) { _forgotPasswordState.postValue(Resource.success(null)); }

            @Override
            public void onError(String msg) { _forgotPasswordState.postValue(Resource.error(msg, null)); }
        });
    }

    private void saveToken(AuthResponse data) {
        if (data != null && data.getAccessToken() != null) {
            sessionManager.saveToken(data.getAccessToken());
            ApiClient.reset();
        }
    }

    private void loadProfileAfterAuth() {
        authRepository.getMyProfile(getApplication(), new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                saveUser(user);
                _registerState.postValue(Resource.success(null));
                _loginSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                _loginMessage.postValue(message);
                _registerState.postValue(Resource.error(message, null));
                _loginSuccess.postValue(false);
            }
        });
    }

    private void saveUser(User user) {
        if (user == null) return;
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
}
