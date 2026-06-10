package com.example.backend.service.impl;

import com.example.backend.dto.request.MatchRequestCreateRequest;
import com.example.backend.dto.request.MatchRequestStatusUpdateRequest;
import com.example.backend.dto.response.MatchRequestResponse;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.Team;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.MatchRequestService;
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
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public MatchRequestServiceImpl(MatchRequestRepository matchRequestRepository,
                                   MatchPostRepository matchPostRepository,
                                   TeamRepository teamRepository,
                                   UserRepository userRepository) {
        this.matchRequestRepository = matchRequestRepository;
        this.matchPostRepository = matchPostRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
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

        return toResponse(matchRequestRepository.save(matchRequest));
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
        return toResponse(matchRequestRepository.save(matchRequest));
    }

    private void acceptRequest(MatchPost post, MatchRequest acceptedRequest) {
        if (post.getStatus() != Enums.PostStatus.OPEN) {
            throw new AppException(400, "Only open match posts can accept a request");
        }

        acceptedRequest.setStatus(Enums.RequestStatus.ACCEPTED);
        matchRequestRepository.save(acceptedRequest);

        List<MatchRequest> pendingRequests = matchRequestRepository.findByPostIdAndStatus(post.getId(), Enums.RequestStatus.PENDING);
        pendingRequests.stream()
                .filter(request -> !request.getId().equals(acceptedRequest.getId()))
                .forEach(request -> request.setStatus(Enums.RequestStatus.REJECTED));
        matchRequestRepository.saveAll(pendingRequests);

        post.setStatus(Enums.PostStatus.MATCHED);
        matchPostRepository.save(post);
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
        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            return true;
        }
        if (post.getTeamId() == null) {
            return false;
        }

        return teamRepository.findById(post.getTeamId())
                .map(Team::getCaptainId)
                .filter(captainId -> captainId.equals(currentUserId))
                .isPresent();
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
        response.setPostTeamId(post != null ? post.getTeamId() : null);
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
