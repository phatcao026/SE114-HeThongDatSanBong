package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MatchPostRequest;
import com.example.timsanbong.data.model.MatchRequestResponse;
import com.example.timsanbong.data.model.RecommendedMatch;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {

    private static final String PREF_MATCHES = "auth";
    private static final String KEY_ACCEPTED_IDS = "accepted_match_ids";

    private String getAcceptedIdsKey(Context context) {
        long userId = new com.example.timsanbong.utils.SessionManager(context).getUserId();
        return KEY_ACCEPTED_IDS + "_" + userId;
    }

    public void saveAcceptedMatchId(Context context, long matchId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_MATCHES, Context.MODE_PRIVATE);
        String key = getAcceptedIdsKey(context);
        Set<String> ids = new HashSet<>(prefs.getStringSet(key, new HashSet<>()));
        ids.add(String.valueOf(matchId));
        prefs.edit().putStringSet(key, ids).apply();
    }

    public Set<Long> getAcceptedMatchIds(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_MATCHES, Context.MODE_PRIVATE);
        String key = getAcceptedIdsKey(context);
        Set<String> idsStr = prefs.getStringSet(key, new HashSet<>());
        Set<Long> ids = new HashSet<>();
        for (String s : idsStr) {
            try {
                ids.add(Long.parseLong(s));
            } catch (Exception ignored) {}
        }
        return ids;
    }

    public void getMatchPosts(Context context, String postType, RepositoryCallback<List<MatchPost>> callback) {
        ApiClient.getService(context).getMatchPosts(postType).enqueue(new Callback<List<MatchPost>>() {
            @Override
            public void onResponse(Call<List<MatchPost>> call, Response<List<MatchPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải danh sách kèo đấu.");
                }
            }

            @Override
            public void onFailure(Call<List<MatchPost>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void getMyMatchPosts(Context context, RepositoryCallback<List<MatchPost>> callback) {
        ApiClient.getService(context).getMyMatchPosts().enqueue(new Callback<List<MatchPost>>() {
            @Override
            public void onResponse(Call<List<MatchPost>> call, Response<List<MatchPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải bài đăng của tôi.");
                }
            }

            @Override
            public void onFailure(Call<List<MatchPost>> call, Throwable t) {
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

    public void createMatchRequest(Context context, long postId, String message,
                                   RepositoryCallback<MatchRequestResponse> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        ApiClient.getService(context).createMatchRequest(postId, body).enqueue(new Callback<MatchRequestResponse>() {
            @Override
            public void onResponse(Call<MatchRequestResponse> call, Response<MatchRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể bắt kèo.");
                }
            }

            @Override
            public void onFailure(Call<MatchRequestResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void deleteMatchPost(Context context, long postId, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).deleteMatchPost(postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Không thể gỡ kèo.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void getSmartRecommendations(Context context, String playstyleNote, String teamName, String date,
                                        String timeStart, String timeEnd, String skillLevel,
                                        Boolean hasField, String postType, String position,
                                        RepositoryCallback<List<RecommendedMatch>> callback) {
        ApiClient.getService(context).getSmartRecommendations(playstyleNote, teamName, date, timeStart, timeEnd,
                skillLevel, hasField, postType, position).enqueue(new Callback<List<RecommendedMatch>>() {
            @Override
            public void onResponse(Call<List<RecommendedMatch>> call, Response<List<RecommendedMatch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể lấy gợi ý kèo.");
                }
            }

            @Override
            public void onFailure(Call<List<RecommendedMatch>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
