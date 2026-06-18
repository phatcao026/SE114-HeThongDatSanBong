package com.example.backend.service.impl;

import com.example.backend.dto.request.MatchRequestCreateRequest;
import com.example.backend.dto.request.MatchRequestStatusUpdateRequest;
import com.example.backend.dto.response.MatchRequestResponse;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ConversationService;
import com.example.backend.service.MatchRequestService;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MatchRequestServiceImpl implements MatchRequestService {
    private final MatchRequestRepository matchRequestRepository;
    private final MatchPostRepository matchPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ConversationService conversationService;

    public MatchRequestServiceImpl(MatchRequestRepository matchRequestRepository,
                                   MatchPostRepository matchPostRepository,
                                   UserRepository userRepository,
                                   NotificationService notificationService,
                                   ConversationService conversationService) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchPostRepository = matchPostRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.conversationService = conversationService;
    }

    @Override
    public List<MatchRequestResponse> getRequestsByPost(Long postId) {
        MatchPost post = findPost(postId);
        ensureCanManagePost(post);
        return toResponses(matchRequestRepository.findByPostIdOrderByCreatedAtDesc(postId));
    }

    @Override
    public List<MatchRequestResponse> getMyRequests() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        return toResponses(matchRequestRepository.findByRequesterIdOrderByCreatedAtDesc(currentUserId));
    }

    @Override
    @Transactional
    public MatchRequestResponse createRequest(Long postId, MatchRequestCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        MatchPost post = findPost(postId);
        if (post.getStatus() != Enums.PostStatus.OPEN) {
            throw new AppException(400, "Only open match posts can receive requests");
        }
        if (canManagePost(post, currentUserId)) {
            throw new AppException(400, "You cannot request your own match post");
        }
        if (matchRequestRepository.existsByPostIdAndRequesterId(postId, currentUserId)) {
            throw new AppException(409, "You have already requested this match post");
        }

        MatchRequest matchRequest = new MatchRequest();
        matchRequest.setPostId(postId);
        matchRequest.setRequesterId(currentUserId);
        matchRequest.setMessage(cleanOptional(request.getMessage()));
        matchRequest.setStatus(Enums.RequestStatus.PENDING);
        matchRequest.setCreatedAt(LocalDateTime.now());

        MatchRequest savedRequest = matchRequestRepository.save(matchRequest);
        notifyNewMatchRequest(post, savedRequest);
        return toResponse(savedRequest);
    }

    @Override
    @Transactional
    public MatchRequestResponse updateRequestStatus(Long requestId, MatchRequestStatusUpdateRequest request) {
        if (request.getStatus() != Enums.RequestStatus.ACCEPTED
                && request.getStatus() != Enums.RequestStatus.REJECTED) {
            throw new AppException(400, "Only ACCEPTED or REJECTED status is allowed");
        }

        MatchRequest matchRequest = findRequest(requestId);
        MatchPost post = findPost(matchRequest.getPostId());
        ensureCanManagePost(post);

        if (matchRequest.getStatus() != Enums.RequestStatus.PENDING) {
            if (matchRequest.getStatus() == request.getStatus()) {
                return toResponse(matchRequest);
            }
            throw new AppException(400, "Only pending requests can be updated");
        }

        if (request.getStatus() == Enums.RequestStatus.ACCEPTED) {
            acceptRequest(post, matchRequest);
            return toResponse(matchRequest);
        }

        matchRequest.setStatus(Enums.RequestStatus.REJECTED);
        MatchRequest savedRequest = matchRequestRepository.save(matchRequest);
        notifyMatchRequestStatus(savedRequest, Enums.RequestStatus.REJECTED);
        return toResponse(savedRequest);
    }

    private void acceptRequest(MatchPost post, MatchRequest acceptedRequest) {
        if (post.getStatus() != Enums.PostStatus.OPEN) {
            throw new AppException(400, "Only open match posts can accept a request");
        }

        acceptedRequest.setStatus(Enums.RequestStatus.ACCEPTED);
        matchRequestRepository.save(acceptedRequest);
        notifyMatchRequestStatus(acceptedRequest, Enums.RequestStatus.ACCEPTED);

        // Cập nhật/Tạo phòng chat nhóm
        if (post.getConversationId() == null) {
            com.example.backend.dto.response.ConversationResponse conv =
                    conversationService.createMatchConversation(post.getUserId(), acceptedRequest.getRequesterId());
            post.setConversationId(conv.getId());
        } else {
            conversationService.addMemberToConversation(post.getConversationId(), acceptedRequest.getRequesterId());
        }

        // Tăng số lượng thành viên đã ghép thành công
        int joined = post.getJoinedMembers() != null ? post.getJoinedMembers() : 0;
        int needed = post.getNeededMembers() != null ? post.getNeededMembers() : 1;
        joined++;
        post.setJoinedMembers(joined);

        // Nếu đã đủ số lượng, chuyển trạng thái bài đăng và từ chối các yêu cầu khác
        if (joined >= needed) {
            post.setStatus(Enums.PostStatus.MATCHED);

            List<MatchRequest> pendingRequests = matchRequestRepository.findByPostIdAndStatus(post.getId(), Enums.RequestStatus.PENDING);
            pendingRequests.stream()
                    .filter(request -> !request.getId().equals(acceptedRequest.getId()))
                    .forEach(request -> request.setStatus(Enums.RequestStatus.REJECTED));
            matchRequestRepository.saveAll(pendingRequests);
            pendingRequests.stream()
                    .filter(request -> !request.getId().equals(acceptedRequest.getId()))
                    .forEach(request -> notifyMatchRequestStatus(request, Enums.RequestStatus.REJECTED));
        }

        matchPostRepository.save(post);
    }

    private void notifyNewMatchRequest(MatchPost post, MatchRequest request) {
        if (post.getUserId() == null) {
            return;
        }

        notificationService.createNotification(
                post.getUserId(),
                "New match request",
                "A player sent a request for your match post #" + post.getId(),
                Enums.NotificationType.MATCH_REQUEST
        );
    }

    private void notifyMatchRequestStatus(MatchRequest request, Enums.RequestStatus status) {
        if (request.getRequesterId() == null) {
            return;
        }

        String title = status == Enums.RequestStatus.ACCEPTED
                ? "Match request accepted"
                : "Match request rejected";
        String content = status == Enums.RequestStatus.ACCEPTED
                ? "Your request for match post #" + request.getPostId() + " was accepted"
                : "Your request for match post #" + request.getPostId() + " was rejected";

        notificationService.createNotification(
                request.getRequesterId(),
                title,
                content,
                Enums.NotificationType.MATCH_REQUEST
        );
    }

    private MatchPost findPost(Long id) {
        return matchPostRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Match post not found"));
    }

    private MatchRequest findRequest(Long id) {
        return matchRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Match request not found"));
    }

    private void ensureCanManagePost(MatchPost post) {
        if (TokenUtils.hasRole("ADMIN")) {
            return;
        }

        Long currentUserId = TokenUtils.getCurrentUserId();
        if (canManagePost(post, currentUserId)) {
            return;
        }

        throw new AppException(403, "Only the post owner can manage match requests");
    }

    private boolean canManagePost(MatchPost post, Long currentUserId) {
        return post.getUserId() != null && post.getUserId().equals(currentUserId);
    }

    private MatchRequestResponse toResponse(MatchRequest request) {
        Map<Long, User> users = mapById(userRepository.findAllById(nonNullList(request.getRequesterId())), User::getId);
        Map<Long, MatchPost> posts = mapById(matchPostRepository.findAllById(nonNullList(request.getPostId())), MatchPost::getId);
        return toResponse(request, users, posts);
    }

    private List<MatchRequestResponse> toResponses(List<MatchRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        Map<Long, User> users = mapById(userRepository.findAllById(collectIds(requests, MatchRequest::getRequesterId)), User::getId);
        Map<Long, MatchPost> posts = mapById(matchPostRepository.findAllById(collectIds(requests, MatchRequest::getPostId)), MatchPost::getId);

        return requests.stream()
                .map(request -> toResponse(request, users, posts))
                .toList();
    }

    private MatchRequestResponse toResponse(MatchRequest request,
                                            Map<Long, User> users,
                                            Map<Long, MatchPost> posts) {
        MatchPost post = posts.get(request.getPostId());

        MatchRequestResponse response = new MatchRequestResponse();
        response.setId(request.getId());
        response.setPostId(request.getPostId());
        response.setRequesterId(request.getRequesterId());
        response.setRequesterName(users.containsKey(request.getRequesterId()) ? users.get(request.getRequesterId()).getFullName() : null);
        response.setMessage(request.getMessage());
        response.setStatus(request.getStatus());
        response.setPostStatus(post != null ? post.getStatus() : null);
        response.setPostOwnerId(post != null ? post.getUserId() : null);
        response.setPostDate(post != null ? post.getDate() : null);
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }

    private <T> Map<Long, T> mapById(List<T> items, Function<T, Long> idExtractor) {
        if (items.isEmpty()) {
            return Collections.emptyMap();
        }

        return items.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
    }

    private List<Long> nonNullList(Long id) {
        if (id == null) {
            return List.of();
        }

        return List.of(id);
    }

    private List<Long> collectIds(Collection<MatchRequest> requests, Function<MatchRequest, Long> extractor) {
        return requests.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
