package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.AppNotification;
import com.example.timsanbong.data.model.UnreadCountResponse;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {

    public void getUnreadCount(Context context, RepositoryCallback<Integer> callback) {
        ApiClient.getService(context).getUnreadNotificationCount().enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getCount());
                } else {
                    callback.onError("Khong the tai so thong bao moi.");
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                callback.onError("Loi ket noi.");
            }
        });
    }

    public void getNotifications(Context context, RepositoryCallback<List<AppNotification>> callback) {
        ApiClient.getService(context).getNotifications(null).enqueue(new Callback<List<AppNotification>>() {
            @Override
            public void onResponse(Call<List<AppNotification>> call, Response<List<AppNotification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải thông báo.");
                }
            }

            @Override
            public void onFailure(Call<List<AppNotification>> call, Throwable t) {
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
