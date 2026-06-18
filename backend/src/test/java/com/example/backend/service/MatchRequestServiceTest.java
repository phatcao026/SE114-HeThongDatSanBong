package com.example.backend.service;

import com.example.backend.dto.request.MatchRequestStatusUpdateRequest;
import com.example.backend.dto.response.ConversationResponse;
import com.example.backend.dto.response.MatchRequestResponse;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.MatchRequestServiceImpl;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchRequestServiceTest {

    @Mock
    private MatchRequestRepository matchRequestRepository;

    @Mock
    private MatchPostRepository matchPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private MatchRequestServiceImpl matchRequestService;

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
    public void testAcceptRequest_FindOpponent_Success() {
        Long ownerId = 1L;
        Long requesterId = 2L;
        Long postId = 10L;
        Long requestId = 100L;

        mockAuthentication(ownerId);

        MatchPost post = new MatchPost();
        post.setId(postId);
        post.setUserId(ownerId);
        post.setPostType(Enums.PostType.FIND_OPPONENT);
        post.setStatus(Enums.PostStatus.OPEN);
        post.setNeededMembers(1);
        post.setJoinedMembers(0);

        MatchRequest request = new MatchRequest();
        request.setId(requestId);
        request.setPostId(postId);
        request.setRequesterId(requesterId);
        request.setStatus(Enums.RequestStatus.PENDING);

        MatchRequest otherRequest = new MatchRequest();
        otherRequest.setId(101L);
        otherRequest.setPostId(postId);
        otherRequest.setRequesterId(3L);
        otherRequest.setStatus(Enums.RequestStatus.PENDING);

        when(matchRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(matchPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(matchRequestRepository.findByPostIdAndStatus(postId, Enums.RequestStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(request, otherRequest)));

        ConversationResponse convResponse = new ConversationResponse();
        convResponse.setId(500L);
        when(conversationService.createMatchConversation(ownerId, requesterId)).thenReturn(convResponse);

        MatchRequestStatusUpdateRequest updateRequest = new MatchRequestStatusUpdateRequest();
        updateRequest.setStatus(Enums.RequestStatus.ACCEPTED);

        MatchRequestResponse response = matchRequestService.updateRequestStatus(requestId, updateRequest);

        assertNotNull(response);
        assertEquals(Enums.RequestStatus.ACCEPTED, request.getStatus());
        assertEquals(Enums.RequestStatus.REJECTED, otherRequest.getStatus());
        assertEquals(Enums.PostStatus.MATCHED, post.getStatus());
        assertEquals(1, post.getJoinedMembers());
        assertEquals(500L, post.getConversationId());

        verify(matchRequestRepository, times(1)).save(request);
        verify(matchRequestRepository, times(1)).saveAll(anyList());
        verify(matchPostRepository, times(1)).save(post);
        verify(conversationService, times(1)).createMatchConversation(ownerId, requesterId);
        verify(conversationService, never()).addMemberToConversation(anyLong(), anyLong());
    }

    @Test
    public void testAcceptRequest_FindMember_FirstAccept_Success() {
        Long ownerId = 1L;
        Long requesterId = 2L;
        Long postId = 10L;
        Long requestId = 100L;

        mockAuthentication(ownerId);

        MatchPost post = new MatchPost();
        post.setId(postId);
        post.setUserId(ownerId);
        post.setPostType(Enums.PostType.FIND_MEMBER);
        post.setStatus(Enums.PostStatus.OPEN);
        post.setNeededMembers(3);
        post.setJoinedMembers(0);

        MatchRequest request = new MatchRequest();
        request.setId(requestId);
        request.setPostId(postId);
        request.setRequesterId(requesterId);
        request.setStatus(Enums.RequestStatus.PENDING);

        when(matchRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(matchPostRepository.findById(postId)).thenReturn(Optional.of(post));

        ConversationResponse convResponse = new ConversationResponse();
        convResponse.setId(500L);
        when(conversationService.createMatchConversation(ownerId, requesterId)).thenReturn(convResponse);

        MatchRequestStatusUpdateRequest updateRequest = new MatchRequestStatusUpdateRequest();
        updateRequest.setStatus(Enums.RequestStatus.ACCEPTED);

        MatchRequestResponse response = matchRequestService.updateRequestStatus(requestId, updateRequest);

        assertNotNull(response);
        assertEquals(Enums.RequestStatus.ACCEPTED, request.getStatus());
        assertEquals(Enums.PostStatus.OPEN, post.getStatus()); // remains OPEN
        assertEquals(1, post.getJoinedMembers());
        assertEquals(500L, post.getConversationId());

        verify(matchRequestRepository, times(1)).save(request);
        verify(matchRequestRepository, never()).saveAll(anyList()); // no auto rejection yet
        verify(matchPostRepository, times(1)).save(post);
        verify(conversationService, times(1)).createMatchConversation(ownerId, requesterId);
    }

    @Test
    public void testAcceptRequest_FindMember_ThresholdReached_Success() {
        Long ownerId = 1L;
        Long requesterId = 4L;
        Long postId = 10L;
        Long requestId = 103L;

        mockAuthentication(ownerId);

        MatchPost post = new MatchPost();
        post.setId(postId);
        post.setUserId(ownerId);
        post.setPostType(Enums.PostType.FIND_MEMBER);
        post.setStatus(Enums.PostStatus.OPEN);
        post.setNeededMembers(3);
        post.setJoinedMembers(2);
        post.setConversationId(500L); // group chat already created

        MatchRequest request = new MatchRequest();
        request.setId(requestId);
        request.setPostId(postId);
        request.setRequesterId(requesterId);
        request.setStatus(Enums.RequestStatus.PENDING);

        MatchRequest otherPending = new MatchRequest();
        otherPending.setId(104L);
        otherPending.setPostId(postId);
        otherPending.setRequesterId(5L);
        otherPending.setStatus(Enums.RequestStatus.PENDING);

        when(matchRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(matchPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(matchRequestRepository.findByPostIdAndStatus(postId, Enums.RequestStatus.PENDING))
                .thenReturn(new ArrayList<>(List.of(request, otherPending)));

        MatchRequestStatusUpdateRequest updateRequest = new MatchRequestStatusUpdateRequest();
        updateRequest.setStatus(Enums.RequestStatus.ACCEPTED);

        MatchRequestResponse response = matchRequestService.updateRequestStatus(requestId, updateRequest);

        assertNotNull(response);
        assertEquals(Enums.RequestStatus.ACCEPTED, request.getStatus());
        assertEquals(Enums.RequestStatus.REJECTED, otherPending.getStatus());
        assertEquals(Enums.PostStatus.MATCHED, post.getStatus()); // Closed!
        assertEquals(3, post.getJoinedMembers());

        verify(matchRequestRepository, times(1)).save(request);
        verify(matchRequestRepository, times(1)).saveAll(anyList());
        verify(matchPostRepository, times(1)).save(post);
        verify(conversationService, never()).createMatchConversation(anyLong(), anyLong());
        verify(conversationService, times(1)).addMemberToConversation(500L, requesterId);
    }
}
