package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AuthResponse;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public void register(Context context, Map<String, String> body, RepositoryCallback<AuthResponse> callback) {
        ApiClient.getService(context).register(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Đăng ký thất bại. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void login(Context context, Map<String, String> credentials, RepositoryCallback<AuthResponse> callback) {
        ApiClient.getService(context).login(credentials).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
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
                    callback.onError("Không thể gửi OTP.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
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
                    callback.onError("OTP không hợp lệ.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
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
                    callback.onError("Không thể đặt lại mật khẩu.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void getGoogleUrl(Context context, RepositoryCallback<String> callback) {
        ApiClient.getService(context).getGoogleUrl().enqueue(new Callback<com.example.timsanbong.data.model.GoogleUrlResponse>() {
            @Override
            public void onResponse(Call<com.example.timsanbong.data.model.GoogleUrlResponse> call, Response<com.example.timsanbong.data.model.GoogleUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getUrl());
                } else {
                    callback.onError("Không thể lấy Google URL.");
                }
            }

            @Override
            public void onFailure(Call<com.example.timsanbong.data.model.GoogleUrlResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void googleSync(Context context, String idToken, RepositoryCallback<AuthResponse> callback) {
        Map<String, String> body = new java.util.HashMap<>();
        body.put("idToken", idToken);
        ApiClient.getService(context).googleSync(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Đăng nhập Google thất bại.");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
