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

    private static final String PREF_MATCHES = "match_prefs";
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

    public void fetchAndSyncAcceptedMatches(Context context) {
        ApiClient.getService(context).getMyMatchRequests().enqueue(new Callback<List<MatchRequestResponse>>() {
            @Override
            public void onResponse(Call<List<MatchRequestResponse>> call, Response<List<MatchRequestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MatchRequestResponse req : response.body()) {
                        saveAcceptedMatchId(context, req.getMatchPostId());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MatchRequestResponse>> call, Throwable t) {}
        });
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

    public void getSmartRecommendations(Context context, String teamName, String date,
                                        String timeStart, String timeEnd, String skillLevel,
                                        Boolean hasField, String postType, String ageRange,
                                        String costSharing,
                                        RepositoryCallback<List<RecommendedMatch>> callback) {
        // Instead of calling a smart recommendation API, we fetch all matches and filter locally
        ApiClient.getService(context).getMatchPosts(null).enqueue(new Callback<List<MatchPost>>() {
            @Override
            public void onResponse(Call<List<MatchPost>> call, Response<List<MatchPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MatchPost> allPosts = response.body();
                    List<RecommendedMatch> results = new ArrayList<>();

                    for (MatchPost post : allPosts) {
                        int totalCriteria = 0;
                        int matchedCriteria = 0;
                        List<String> matchesList = new ArrayList<>();

                        if (teamName != null && !teamName.isEmpty()) {
                            totalCriteria++;
                            if (post.getTeamName() != null && post.getTeamName().toLowerCase().contains(teamName.toLowerCase())) {
                                matchedCriteria++;
                                matchesList.add("tên đội");
                            }
                        }
                        if (date != null && !date.isEmpty()) {
                            totalCriteria++;
                            if (date.equals(post.getDate())) {
                                matchedCriteria++;
                                matchesList.add("ngày đá");
                            }
                        }
                        if (skillLevel != null && !skillLevel.isEmpty()) {
                            totalCriteria++;
                            if (skillLevel.equals(post.getSkillLevel())) {
                                matchedCriteria++;
                                matchesList.add("trình độ");
                            }
                        }
                        if (postType != null && !postType.isEmpty()) {
                            totalCriteria++;
                            if (postType.equals(post.getPostType())) {
                                matchedCriteria++;
                                matchesList.add("loại kèo");
                            }
                        }
                        if (ageRange != null && !ageRange.isEmpty()) {
                            totalCriteria++;
                            if (ageRange.equals(post.getAgeRange())) {
                                matchedCriteria++;
                                matchesList.add("độ tuổi");
                            }
                        }
                        if (costSharing != null && !costSharing.isEmpty()) {
                            totalCriteria++;
                            if (costSharing.equals(post.getCost())) {
                                matchedCriteria++;
                                matchesList.add("quy tắc chia tiền");
                            }
                        }
                        if (hasField != null) {
                            totalCriteria++;
                            if (hasField.equals(post.getHasField())) {
                                matchedCriteria++;
                                matchesList.add("trạng thái sân");
                            }
                        }
                        if (timeStart != null && !timeStart.isEmpty() && timeEnd != null && !timeEnd.isEmpty()) {
                            totalCriteria++;
                            String postStart = post.getTimeStart();
                            String postEnd = post.getTimeEnd();
                            if (postStart != null && postEnd != null) {
                                if (postStart.compareTo(timeStart) >= 0 && postEnd.compareTo(timeEnd) <= 0) {
                                    matchedCriteria++;
                                    matchesList.add("khung giờ");
                                }
                            }
                        }

                        if (matchedCriteria > 0) {
                            RecommendedMatch rm = new RecommendedMatch();
                            rm.setMatchId(post.getId());
                            rm.setMatchPost(post);
                            
                            int score = (int) (((double) matchedCriteria / totalCriteria) * 100);
                            rm.setMatchScore(score);
                            
                            StringBuilder sb = new StringBuilder("Gợi ý trùng với: ");
                            for (int i = 0; i < matchesList.size(); i++) {
                                sb.append(matchesList.get(i));
                                if (i < matchesList.size() - 1) sb.append(", ");
                            }
                            rm.setAiExplanation(sb.toString());

                            results.add(rm);
                        }
                    }
                    callback.onSuccess(results);
                } else {
                    callback.onError("Không thể lấy danh sách kèo để tìm kiếm.");
                }
            }

            @Override
            public void onFailure(Call<List<MatchPost>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
