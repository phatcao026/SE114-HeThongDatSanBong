package com.example.backend.service;

import com.example.backend.dto.request.ConversationCreateRequest;
import com.example.backend.dto.request.MessageCreateRequest;
import com.example.backend.dto.response.ConversationResponse;
import com.example.backend.dto.response.MessageResponse;

import java.util.List;

public interface ConversationService {
    List<ConversationResponse> getMyConversations();

    ConversationResponse getConversation(Long id);

    ConversationResponse createDirectConversation(ConversationCreateRequest request);

    ConversationResponse createMatchConversation(Long postOwnerId, Long requesterId);

    List<MessageResponse> getMessages(Long conversationId);

    MessageResponse sendMessage(Long conversationId, MessageCreateRequest request);

    void addMemberToConversation(Long conversationId, Long userId);
}
