package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AppNotification;
import com.example.timsanbong.data.model.PageResponse;
import com.example.timsanbong.utils.RepositoryCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {

    public void getNotifications(Context context, int page, int size, RepositoryCallback<PageResponse<AppNotification>> callback) {
        ApiClient.getService(context).getNotifications(page, size).enqueue(new Callback<PageResponse<AppNotification>>() {
            @Override
            public void onResponse(Call<PageResponse<AppNotification>> call, Response<PageResponse<AppNotification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải thông báo.");
                }
            }

            @Override
            public void onFailure(Call<PageResponse<AppNotification>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void markNotificationRead(Context context, long id, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).markNotificationRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Không thể đánh dấu đã đọc.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void markAllNotificationsRead(Context context, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).markAllNotificationsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Không thể đánh dấu đã đọc.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
