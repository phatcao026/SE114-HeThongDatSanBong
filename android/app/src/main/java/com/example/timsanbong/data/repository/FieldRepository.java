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

public class FieldRepository {

    public void getFields(Context context, RepositoryCallback<List<Field>> callback) {
        ApiClient.getService(context).getFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(getMockFields());
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                callback.onSuccess(getMockFields());
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

    public List<Field> getMockFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field(1, "Sân Bóng A", "123 Đường Lê Lợi, Q1", 150000, "https://via.placeholder.com/300", "Sân cỏ nhân tạo 5 người", "5 người", true));
        fields.add(new Field(2, "Sân Bóng B", "456 Đường Nguyễn Huệ, Q1", 200000, "https://via.placeholder.com/300", "Sân cỏ tự nhiên 7 người", "7 người", true));
        fields.add(new Field(3, "Sân Bóng C", "789 Đường Trần Hưng Đạo, Q5", 250000, "https://via.placeholder.com/300", "Sân cỏ nhân tạo 11 người", "11 người", false));
        fields.add(new Field(4, "Sân Bóng D", "321 Đường Phạm Văn Đồng, Q3", 180000, "https://via.placeholder.com/300", "Sân cỏ tự nhiên 5 người", "5 người", true));
        fields.add(new Field(5, "Sân Bóng E", "654 Đường Cách Mạng Tháng 8, Q10", 220000, "https://via.placeholder.com/300", "Sân cỏ nhân tạo 7 người", "7 người", true));
        return fields;
    }
}
