package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.User;
import com.example.timsanbong.utils.RepositoryCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    public void getMyProfile(Context context, RepositoryCallback<User> callback) {
        ApiClient.getService(context).getMyProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải hồ sơ người dùng.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }
}
