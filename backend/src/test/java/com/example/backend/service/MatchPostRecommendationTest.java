package com.example.backend.service;

import com.example.backend.dto.ai.AiOpponentDto;
import com.example.backend.dto.ai.AiRecommendationResult;
import com.example.backend.dto.response.RecommendedMatchResponse;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.User;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ai.GroqAiService;
import com.example.backend.service.impl.MatchPostServiceImpl;
import com.example.backend.utils.Enums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchPostRecommendationTest {

    @Mock
    private MatchPostRepository matchPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroqAiService groqAiService;

    @InjectMocks
    private MatchPostServiceImpl matchPostService;

    private void mockAuthentication(Long userId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetSmartRecommendations_Success() {
        Long currentUserId = 1L;
        mockAuthentication(currentUserId);

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setTrustScore(90);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        MatchPost post1 = new MatchPost();
        post1.setId(10L);
        post1.setUserId(2L);
        post1.setMessage("Play offensive");
        User user2 = new User();
        user2.setId(2L);
        user2.setTrustScore(95);
        post1.setUser(user2);

        Page<MatchPost> matchPage = new PageImpl<>(List.of(post1));
        when(matchPostRepository.findPotentialMatches(eq(currentUserId), eq(Enums.PostType.FIND_OPPONENT), any())).thenReturn(matchPage);

        AiRecommendationResult aiResult = new AiRecommendationResult(10L, "Highly compatible playstyle");
        when(groqAiService.recommendMatches(
                eq("Fast paced"),
                eq(90),
                any(),
                any(),
                any(),
                any(),
                any(),
                eq(Enums.PostType.FIND_OPPONENT),
                any(),
                anyList()
        )).thenReturn(List.of(aiResult));

        List<RecommendedMatchResponse> recommendations = matchPostService.getSmartRecommendations(
                "Fast paced", null, null, null, null, null, Enums.PostType.FIND_OPPONENT, null
        );

        assertNotNull(recommendations);
        assertEquals(1, recommendations.size());
        assertEquals(10L, recommendations.get(0).getMatchId());
        assertEquals("Play offensive", recommendations.get(0).getOpponentNote());
        assertEquals("Highly compatible playstyle", recommendations.get(0).getAiExplanation());
    }

    @Test
    public void testGetSmartRecommendations_EmptyPotentialMatches() {
        Long currentUserId = 1L;
        mockAuthentication(currentUserId);

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setTrustScore(90);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        when(matchPostRepository.findPotentialMatches(eq(currentUserId), eq(Enums.PostType.FIND_OPPONENT), any())).thenReturn(Page.empty());

        List<RecommendedMatchResponse> recommendations = matchPostService.getSmartRecommendations(
                "Fast paced", null, null, null, null, null, Enums.PostType.FIND_OPPONENT, null
        );

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
        verify(groqAiService, never()).recommendMatches(
                anyString(), anyInt(), any(), any(), any(), any(), any(), any(), any(), anyList()
        );
    }
}
