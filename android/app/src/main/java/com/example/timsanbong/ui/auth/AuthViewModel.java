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

    public void login(String email, String password) {
        _registerState.setValue(Resource.loading(null));

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        authRepository.login(getApplication(), body, new RepositoryCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                if (data.getAccessToken() != null) {
                    sessionManager.saveToken(data.getAccessToken());
                    sessionManager.saveUserId(data.getUserId());
                    sessionManager.saveUserRole(data.getRole());
                    sessionManager.saveUserEmail(data.getEmail());
                }
                _registerState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _loginMessage.postValue(message);
                _registerState.postValue(Resource.error(message, null));
            }
        });
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
                if (data.getAccessToken() != null) {
                    sessionManager.saveToken(data.getAccessToken());
                    sessionManager.saveUserId(data.getUserId());
                    sessionManager.saveUserRole(data.getRole());
                    sessionManager.saveUserEmail(data.getEmail());
                }
                _registerState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _registerState.postValue(Resource.error(message, null));
            }
        });
    }

    // OTP Flow
    private final MutableLiveData<Resource<Void>> _forgotPasswordState = new MutableLiveData<>();
    public LiveData<Resource<Void>> forgotPasswordState = _forgotPasswordState;

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

    private final MutableLiveData<Resource<String>> _googleUrlState = new MutableLiveData<>();
    public LiveData<Resource<String>> googleUrlState = _googleUrlState;

    public void getGoogleUrl() {
        _googleUrlState.setValue(Resource.loading(null));
        authRepository.getGoogleUrl(getApplication(), new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String data) { _googleUrlState.postValue(Resource.success(data)); }
            @Override
            public void onError(String msg) { _googleUrlState.postValue(Resource.error(msg, null)); }
        });
    }

    public void googleSync(String idToken) {
        _registerState.setValue(Resource.loading(null));
        authRepository.googleSync(getApplication(), idToken, new RepositoryCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                if (data.getAccessToken() != null) {
                    sessionManager.saveToken(data.getAccessToken());
                    sessionManager.saveUserId(data.getUserId());
                    sessionManager.saveUserRole(data.getRole());
                    sessionManager.saveUserEmail(data.getEmail());
                }
                _registerState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String msg) {
                _loginMessage.postValue(msg);
                _registerState.postValue(Resource.error(msg, null));
            }
        });
    }
}
