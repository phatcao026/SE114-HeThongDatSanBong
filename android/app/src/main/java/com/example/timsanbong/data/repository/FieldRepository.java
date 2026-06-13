package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.timsanbong.data.model.TimeSlotResponse;

public class FieldRepository {

    public void getFields(Context context, RepositoryCallback<List<Field>> callback) {
        ApiClient.getService(context).getFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải danh sách sân.");
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void getFieldById(Context context, long id, RepositoryCallback<Field> callback) {
        ApiClient.getService(context).getFieldById(id).enqueue(new Callback<Field>() {
            @Override
            public void onResponse(Call<Field> call, Response<Field> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải thông tin sân.");
                }
            }

            @Override
            public void onFailure(Call<Field> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void getTimeslots(Context context, long id, String date, RepositoryCallback<List<TimeSlotResponse>> callback) {
        ApiClient.getService(context).getTimeslots(id, date).enqueue(new Callback<List<TimeSlotResponse>>() {
            @Override
            public void onResponse(Call<List<TimeSlotResponse>> call, Response<List<TimeSlotResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải khung giờ.");
                }
            }

            @Override
            public void onFailure(Call<List<TimeSlotResponse>> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }
}
