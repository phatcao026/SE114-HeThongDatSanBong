package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldCreateRequest;
import com.example.timsanbong.data.model.FieldUpdateRequest;
import com.example.timsanbong.data.model.TimeSlot;
import com.example.timsanbong.data.model.TimeSlotCreateRequest;
import com.example.timsanbong.data.model.TimeSlotUpdateRequest;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerFieldRepository {

    public void getOwnerFields(Context context, RepositoryCallback<List<Field>> callback) {
        long ownerId = getCurrentUserId(context);
        ApiClient.getService(context).getFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Field> ownerFields = new ArrayList<>();
                    for (Field field : response.body()) {
                        if (ownerId <= 0 || field.getOwnerId() == ownerId) {
                            ownerFields.add(field);
                        }
                    }
                    callback.onSuccess(ownerFields);
                } else {
                    callback.onError("Không tải được danh sách sân.");
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void createField(Context context, FieldCreateRequest request, RepositoryCallback<Field> callback) {
        if (!isValidFieldRequest(request.getName(), request.getType())) {
            callback.onError("Vui long nhap ten san va loai san.");
            return;
        }
        ApiClient.getService(context).createOwnerField(request).enqueue(fieldCallback(callback, "Tạo sân không thành công."));
    }

    public void updateField(Context context, long fieldId, FieldUpdateRequest request, RepositoryCallback<Field> callback) {
        if (fieldId <= 0) {
            callback.onError("San khong hop le.");
            return;
        }
        ApiClient.getService(context).updateOwnerField(fieldId, request)
                .enqueue(fieldCallback(callback, "Cập nhật sân không thành công."));
    }

    public void deleteField(Context context, long fieldId, RepositoryCallback<Field> callback) {
        if (fieldId <= 0) {
            callback.onError("San khong hop le.");
            return;
        }
        ApiClient.getService(context).deleteOwnerField(fieldId)
                .enqueue(fieldCallback(callback, "Xóa sân không thành công."));
    }

    public void createTimeSlot(Context context, long fieldId, TimeSlotCreateRequest request,
                               RepositoryCallback<TimeSlot> callback) {
        if (fieldId <= 0) {
            callback.onError("San khong hop le.");
            return;
        }
        if (!isValidTimeSlot(request.getStartTime(), request.getEndTime(), request.getPrice())) {
            callback.onError("Khung gio khong hop le.");
            return;
        }
        ApiClient.getService(context).createOwnerTimeSlot(fieldId, request)
                .enqueue(timeSlotCallback(callback, "Tạo khung giờ không thành công."));
    }

    public void updateTimeSlot(Context context, long fieldId, long slotId, TimeSlotUpdateRequest request,
                               RepositoryCallback<TimeSlot> callback) {
        if (fieldId <= 0 || slotId <= 0) {
            callback.onError("Khung gio khong hop le.");
            return;
        }
        ApiClient.getService(context).updateOwnerTimeSlot(fieldId, slotId, request)
                .enqueue(timeSlotCallback(callback, "Cập nhật khung giờ không thành công."));
    }

    public void deleteTimeSlot(Context context, long fieldId, long slotId, RepositoryCallback<TimeSlot> callback) {
        if (fieldId <= 0 || slotId <= 0) {
            callback.onError("Khung gio khong hop le.");
            return;
        }
        ApiClient.getService(context).deleteOwnerTimeSlot(fieldId, slotId)
                .enqueue(timeSlotCallback(callback, "Xóa khung giờ không thành công."));
    }

    private Callback<Field> fieldCallback(RepositoryCallback<Field> callback, String fallbackMessage) {
        return new Callback<Field>() {
            @Override
            public void onResponse(Call<Field> call, Response<Field> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(fallbackMessage);
                }
            }

            @Override
            public void onFailure(Call<Field> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        };
    }

    private Callback<TimeSlot> timeSlotCallback(RepositoryCallback<TimeSlot> callback, String fallbackMessage) {
        return new Callback<TimeSlot>() {
            @Override
            public void onResponse(Call<TimeSlot> call, Response<TimeSlot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(fallbackMessage);
                }
            }

            @Override
            public void onFailure(Call<TimeSlot> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        };
    }

    private long getCurrentUserId(Context context) {
        try {
            String userJson = new SessionManager(context).getUserJson();
            if (userJson == null) {
                return 0;
            }
            return new JSONObject(userJson).optLong("id", 0);
        } catch (JSONException ignored) {
            return 0;
        }
    }

    private boolean isValidFieldRequest(String name, String type) {
        return name != null && !name.trim().isEmpty()
                && type != null && !type.trim().isEmpty();
    }

    private boolean isValidTimeSlot(String startTime, String endTime, double price) {
        return startTime != null && !startTime.trim().isEmpty()
                && endTime != null && !endTime.trim().isEmpty()
                && price >= 0;
    }
}
