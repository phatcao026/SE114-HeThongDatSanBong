package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AuthResponse;
import com.example.timsanbong.data.model.User;
import com.example.timsanbong.utils.RepositoryCallback;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public void login(Context context, Map<String, String> body, RepositoryCallback<AuthResponse> callback) {
        ApiClient.getService(context).login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getAccessToken() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response, "Invalid email or password."));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    public void register(Context context, Map<String, String> body, RepositoryCallback<AuthResponse> callback) {
        ApiClient.getService(context).register(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getAccessToken() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response, "Registration failed. Please try again."));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    public void sendRegisterOtp(Context context, Map<String, String> body, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).sendRegisterOtp(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response, "Cannot send registration OTP."));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    public void getMyProfile(Context context, RepositoryCallback<User> callback) {
        ApiClient.getService(context).getMyProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response, "Cannot load account profile."));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    public void forgotPassword(Context context, Map<String, String> body, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).forgotPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response, "Cannot send OTP."));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    public void verifyOtp(Context context, Map<String, String> body, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).verifyOtp(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response, "OTP is invalid or expired."));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    public void resetPassword(Context context, Map<String, String> body, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).resetPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(getErrorMessage(response, "Cannot reset password."));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Cannot connect to server.");
            }
        });
    }

    private String getErrorMessage(Response<?> response, String fallback) {
        if (response != null && response.errorBody() != null) {
            try {
                String body = response.errorBody().string();
                if (body != null && !body.isEmpty()) {
                    String message = new JSONObject(body).optString("message");
                    if (message != null && !message.isEmpty()) {
                        return message;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return fallback;
    }
}
