package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.FieldReviewRequest;
import com.example.timsanbong.data.model.FieldReviewResponse;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FieldReviewRepository {

    public void createReview(Context context, FieldReviewRequest request,
                             RepositoryCallback<FieldReviewResponse> callback) {
        ApiClient.getService(context).createFieldReview(request).enqueue(new Callback<FieldReviewResponse>() {
            @Override
            public void onResponse(Call<FieldReviewResponse> call, Response<FieldReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 409) {
                    callback.onError("Bạn đã đánh giá lịch đặt sân này.");
                } else if (response.code() == 400) {
                    callback.onError("Chỉ có thể đánh giá lịch đặt sân đã hoàn thành.");
                } else {
                    callback.onError("Không thể gửi đánh giá sân.");
                }
            }

            @Override
            public void onFailure(Call<FieldReviewResponse> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void getReviewsForField(Context context, long fieldId,
                                   RepositoryCallback<List<FieldReviewResponse>> callback) {
        ApiClient.getService(context).getFieldReviews(fieldId).enqueue(new Callback<List<FieldReviewResponse>>() {
            @Override
            public void onResponse(Call<List<FieldReviewResponse>> call, Response<List<FieldReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải đánh giá sân.");
                }
            }

            @Override
            public void onFailure(Call<List<FieldReviewResponse>> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }
}
