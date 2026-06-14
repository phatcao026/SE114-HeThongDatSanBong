package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void createDirectConversation(Context context, long recipientId, RepositoryCallback<Conversation> callback) {
        Map<String, Long> body = new HashMap<>();
        body.put("recipientId", recipientId);
        ApiClient.getService(context).createDirectConversation(body).enqueue(new Callback<Conversation>() {
            @Override
            public void onResponse(Call<Conversation> call, Response<Conversation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("KhÃ´ng thá»ƒ má»Ÿ cuá»™c trÃ² chuyá»‡n.");
                }
            }

            @Override
            public void onFailure(Call<Conversation> call, Throwable t) {
                callback.onError("Lá»—i káº¿t ná»‘i.");
            }
        });
    }

    public void getMessages(Context context, long conversationId, RepositoryCallback<List<ChatMessage>> callback) {
        ApiClient.getService(context).getMessages(conversationId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không thể tải tin nhắn.");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                callback.onError("Lỗi kết nối.");
            }
        });
    }

    public void sendMessage(Context context, MessageRequest request, RepositoryCallback<ChatMessage> callback) {
        ApiClient.getService(context).sendMessage(request.getConversationId(), request).enqueue(new Callback<ChatMessage>() {
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
