package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.data.model.PageResponse;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    public void getConversations(Context context, RepositoryCallback<List<Conversation>> callback) {
        ApiClient.getService(context).getConversations().enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải danh sách tin nhắn.");
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void getMessages(Context context, long conversationId, int page, int size, RepositoryCallback<PageResponse<ChatMessage>> callback) {
        ApiClient.getService(context).getMessages(conversationId, page, size).enqueue(new Callback<PageResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<PageResponse<ChatMessage>> call, Response<PageResponse<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải tin nhắn.");
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ChatMessage>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void sendMessage(Context context, MessageRequest request, RepositoryCallback<ChatMessage> callback) {
        ApiClient.getService(context).sendMessage(request).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Gửi tin nhắn thất bại.");
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }
}
