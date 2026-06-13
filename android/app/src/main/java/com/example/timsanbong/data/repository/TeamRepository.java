package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.TeamRequest;
import com.example.timsanbong.data.model.TeamResponse;
import com.example.timsanbong.data.model.InvitationResponse;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamRepository {

    public void getMyTeams(Context context, RepositoryCallback<List<TeamResponse>> callback) {
        ApiClient.getService(context).getMyTeams().enqueue(new Callback<List<TeamResponse>>() {
            @Override
            public void onResponse(Call<List<TeamResponse>> call, Response<List<TeamResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải danh sách đội.");
                }
            }

            @Override
            public void onFailure(Call<List<TeamResponse>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void createTeam(Context context, TeamRequest request, RepositoryCallback<TeamResponse> callback) {
        ApiClient.getService(context).createTeam(request).enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tạo đội.");
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
    
    public void getInvitations(Context context, RepositoryCallback<List<InvitationResponse>> callback) {
        ApiClient.getService(context).getInvitations().enqueue(new Callback<List<InvitationResponse>>() {
            @Override
            public void onResponse(Call<List<InvitationResponse>> call, Response<List<InvitationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải lời mời.");
                }
            }

            @Override
            public void onFailure(Call<List<InvitationResponse>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
