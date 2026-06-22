package com.example.timsanbong.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldCreateRequest;
import com.example.timsanbong.data.model.FieldUpdateRequest;
import com.example.timsanbong.data.model.TimeSlot;
import com.example.timsanbong.data.model.TimeSlotCreateRequest;
import com.example.timsanbong.data.model.TimeSlotResponse;
import com.example.timsanbong.data.model.TimeSlotUpdateRequest;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerFieldRepository {

    public void uploadImage(Context context, okhttp3.MultipartBody.Part filePart, RepositoryCallback<String> callback) {
        ApiClient.getService(context).uploadImage(filePart).enqueue(new Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().get("imageUrl"));
                } else {
                    callback.onError("Upload ảnh không thành công.");
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                callback.onError("Lỗi kết nối khi upload ảnh: " + t.getMessage());
            }
        });
    }

    public void getOwnerFields(Context context, String date, RepositoryCallback<List<Field>> callback) {
        android.util.Log.d("API_DEBUG", "Calling getOwnerFields with date: " + date);

        ApiClient.getService(context).getOwnerFields(date).enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(@NonNull Call<List<Field>> call, @NonNull Response<List<Field>> response) {
                android.util.Log.d("API_DEBUG", "Full URL: " + call.request().url().toString());

                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    android.util.Log.e("API_DEBUG", "Error Code: " + response.code());

                    callback.onError("Không tải được danh sách sân.");
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ: " + t.getMessage());
            }
        });
    }

    public void createField(Context context, FieldCreateRequest request, RepositoryCallback<Field> callback) {
        ApiClient.getService(context).createOwnerField(request).enqueue(new Callback<Field>() {
            @Override
            public void onResponse(Call<Field> call, Response<Field> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Tạo sân không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Field> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void updateField(Context context, long fieldId, FieldUpdateRequest request, RepositoryCallback<Field> callback) {
        ApiClient.getService(context).updateOwnerField(fieldId, request).enqueue(new Callback<Field>() {
            @Override
            public void onResponse(Call<Field> call, Response<Field> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Cập nhật sân không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Field> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void deleteField(Context context, long fieldId, RepositoryCallback<Field> callback) {
        ApiClient.getService(context).deleteOwnerField(fieldId).enqueue(new Callback<Field>() {
            @Override
            public void onResponse(Call<Field> call, Response<Field> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Xóa sân không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Field> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void createTimeSlot(Context context, long fieldId, TimeSlotCreateRequest request, RepositoryCallback<TimeSlot> callback) {
        ApiClient.getService(context).createOwnerTimeSlot(fieldId, request).enqueue(new Callback<TimeSlot>() {
            @Override
            public void onResponse(Call<TimeSlot> call, Response<TimeSlot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Tạo khung giờ không thành công.");
                }
            }

            @Override
            public void onFailure(Call<TimeSlot> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void updateTimeSlot(Context context, long fieldId, long slotId, TimeSlotUpdateRequest request, RepositoryCallback<TimeSlot> callback) {
        ApiClient.getService(context).updateOwnerTimeSlot(fieldId, slotId, request).enqueue(new Callback<TimeSlot>() {
            @Override
            public void onResponse(Call<TimeSlot> call, Response<TimeSlot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Cập nhật khung giờ không thành công.");
                }
            }

            @Override
            public void onFailure(Call<TimeSlot> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void deleteTimeSlot(Context context, long fieldId, long slotId, RepositoryCallback<TimeSlot> callback) {
        ApiClient.getService(context).deleteOwnerTimeSlot(fieldId, slotId).enqueue(new Callback<TimeSlot>() {
            @Override
            public void onResponse(Call<TimeSlot> call, Response<TimeSlot> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Xóa khung giờ không thành công.");
                }
            }

            @Override
            public void onFailure(Call<TimeSlot> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void getFieldAvailability(Context context, long fieldId, String date, RepositoryCallback<List<TimeSlotResponse>> callback) {
        ApiClient.getService(context).getFieldAvailability(fieldId, date).enqueue(new Callback<List<TimeSlotResponse>>() {
            @Override
            public void onResponse(Call<List<TimeSlotResponse>> call, Response<List<TimeSlotResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tải được thông tin khung giờ cho ngày " + date);
                }
            }

            @Override
            public void onFailure(Call<List<TimeSlotResponse>> call, Throwable t) {
                callback.onError("Lỗi kết nối máy chủ khi tải khung giờ.");
            }
        });
    }
}
