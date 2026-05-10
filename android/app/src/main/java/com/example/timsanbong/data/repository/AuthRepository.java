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
}
