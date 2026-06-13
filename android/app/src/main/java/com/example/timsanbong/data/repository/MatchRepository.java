package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.PageResponse;
import com.example.timsanbong.data.model.MatchPostRequest;
import com.example.timsanbong.utils.RepositoryCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {

    public void getMatchPosts(Context context, int page, int size, String postType, RepositoryCallback<PageResponse<MatchPost>> callback) {
        ApiClient.getService(context).getMatchPosts(page, size, postType).enqueue(new Callback<PageResponse<MatchPost>>() {
            @Override
            public void onResponse(Call<PageResponse<MatchPost>> call, Response<PageResponse<MatchPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải danh sách kèo đấu.");
                }
            }

            @Override
            public void onFailure(Call<PageResponse<MatchPost>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void createMatchPost(Context context, MatchPostRequest request, RepositoryCallback<MatchPost> callback) {
        ApiClient.getService(context).createMatchPost(request).enqueue(new Callback<MatchPost>() {
            @Override
            public void onResponse(Call<MatchPost> call, Response<MatchPost> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tạo kèo đấu.");
                }
            }

            @Override
            public void onFailure(Call<MatchPost> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
