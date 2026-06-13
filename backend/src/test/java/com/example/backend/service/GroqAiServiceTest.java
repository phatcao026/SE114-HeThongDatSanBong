package com.example.backend.service;

import com.example.backend.dto.request.ChatCreateRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.entity.Field;
import com.example.backend.entity.redis.ChatSession;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.redis.ChatSessionRepository;
import com.example.backend.service.ai.GroqAiService;
import com.example.backend.utils.Enums;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroqAiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private FieldRepository fieldRepository;

    @InjectMocks
    private GroqAiService groqAiService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(groqAiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(groqAiService, "apiUrl", "http://test-url");
        ReflectionTestUtils.setField(groqAiService, "model", "test-model");
    }

    @Test
    public void testAskChatbot_Success_NewSession() throws Exception {
        ChatCreateRequest request = new ChatCreateRequest();
        request.setMessage("Hello");

        Field field1 = new Field();
        field1.setId(1L);
        field1.setName("San A");
        field1.setType(Enums.FieldType.FIVE_A_SIDE);

        when(fieldRepository.findAll()).thenReturn(List.of(field1));
        when(chatSessionRepository.findById(anyString())).thenReturn(Optional.empty());

        // Mock Groq API response JSON structure
        String mockResponse = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Hello, how can I help you?\"}}]}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(mockResponse);

        ChatResponse response = groqAiService.askChatbot(request);

        assertNotNull(response);
        assertEquals("Hello, how can I help you?", response.getReply());
        assertNotNull(response.getSessionId());

        verify(chatSessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    public void testAskChatbot_Success_ExistingSession() throws Exception {
        String sessionId = "existing-session-123";
        ChatCreateRequest request = new ChatCreateRequest();
        request.setMessage("How much is field booking?");
        request.setSessionId(sessionId);

        ChatSession existingSession = new ChatSession(sessionId, new ArrayList<>());
        existingSession.getMessages().add(new ChatSession.MessageNode("user", "Hi"));
        existingSession.getMessages().add(new ChatSession.MessageNode("assistant", "Hello!"));

        when(fieldRepository.findAll()).thenReturn(List.of());
        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));

        String mockResponse = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"It costs 200k.\"}}]}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(mockResponse);

        ChatResponse response = groqAiService.askChatbot(request);

        assertNotNull(response);
        assertEquals("It costs 200k.", response.getReply());
        assertEquals(sessionId, response.getSessionId());

        verify(chatSessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    public void testAskChatbot_ApiError_ReturnsSystemErrorResponse() {
        ChatCreateRequest request = new ChatCreateRequest();
        request.setMessage("Hello");

        when(fieldRepository.findAll()).thenReturn(List.of());
        when(chatSessionRepository.findById(anyString())).thenReturn(Optional.empty());
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API Call Failed"));

        ChatResponse response = groqAiService.askChatbot(request);

        assertNotNull(response);
        assertTrue(response.getReply().startsWith("Lỗi hệ thống:"));
    }
}
