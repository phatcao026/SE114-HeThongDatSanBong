package com.example.timsanbong.network;

import com.example.timsanbong.data.chat.ChatCreateRequest;
import com.example.timsanbong.data.chat.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit API for the chatbot endpoint.
 * Expects Authorization: Bearer <token> header.
 */
public interface ChatApiService {
    @POST("/api/chat/ask")
    Call<ChatResponse> ask(@Header("Authorization") String bearerToken, @Body ChatCreateRequest request);
}

