package com.example.backend.controller;

import com.example.backend.dto.request.ChatCreateRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.service.ai.GroqAiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GroqAiService groqAiService;

    public ChatController(GroqAiService groqAiService) {
        this.groqAiService = groqAiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askChatbot(@Valid @RequestBody ChatCreateRequest request) {
        ChatResponse response = groqAiService.askChatbot(request);
        return ResponseEntity.ok(response);
    }
}
