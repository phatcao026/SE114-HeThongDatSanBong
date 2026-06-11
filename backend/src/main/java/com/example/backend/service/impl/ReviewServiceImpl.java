package com.example.backend.service.impl;

import com.example.backend.dto.request.ReviewCreateRequest;
import com.example.backend.dto.request.ReviewStatusUpdateRequest;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.Review;
import com.example.backend.entity.Team;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ReviewService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReviewServiceImpl implements ReviewService {
    private static final int PENDING_REVIEW_SCORE_THRESHOLD = -10;

    private final ReviewRepository reviewRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final MatchPostRepository matchPostRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             MatchRequestRepository matchRequestRepository,
                             MatchPostRepository matchPostRepository,
                             TeamRepository teamRepository,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.matchPostRepository = matchPostRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        MatchRequest matchRequest = findMatchRequest(request.getMatchRequestId());
        if (matchRequest.getStatus() != Enums.RequestStatus.ACCEPTED) {
            throw new AppException(400, "Only accepted match requests can be reviewed");
        }

        MatchPost post = findMatchPost(matchRequest.getPostId());
        Long revieweeId = resolveRevieweeId(matchRequest, post, currentUserId);
        if (revieweeId.equals(currentUserId)) {
            throw new AppException(400, "You cannot review yourself");
        }
        if (reviewRepository.existsByMatchRequestIdAndReviewerId(matchRequest.getId(), currentUserId)) {
            throw new AppException(409, "You have already reviewed this match request");
        }

        Review review = new Review();
        review.setReviewerId(currentUserId);
        review.setRevieweeId(revieweeId);
        review.setMatchRequestId(matchRequest.getId());
        review.setScoreChange(request.getScoreChange());
        review.setReason(cleanRequired(request.getReason(), "Reason is required"));
        review.setAiSuggestedPenalty(request.getScoreChange() < 0 ? request.getScoreChange() : null);
        review.setStatus(resolveInitialStatus(request.getScoreChange()));
        review.setCreatedAt(LocalDateTime.now());

        try {
            Review savedReview = reviewRepository.saveAndFlush(review);
            if (isScoreApplied(savedReview.getStatus())) {
                applyTrustScore(savedReview.getRevieweeId(), savedReview.getScoreChange());
            }
            return toResponse(savedReview);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(409, "You have already reviewed this match request");
        }
    }

    @Override
    public List<ReviewResponse> getWrittenReviews() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return toResponses(reviewRepository.findByReviewerIdOrderByCreatedAtDesc(currentUserId));
    }

    @Override
    public List<ReviewResponse> getReceivedReviews() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return toResponses(reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(currentUserId));
    }

    @Override
    public List<ReviewResponse> getReviewsByMatchRequest(Long matchRequestId) {
        MatchRequest matchRequest = findMatchRequest(matchRequestId);
        MatchPost post = findMatchPost(matchRequest.getPostId());
        ensureCanViewMatchRequestReviews(matchRequest, post);
        return toResponses(reviewRepository.findByMatchRequestIdOrderByCreatedAtDesc(matchRequestId));
    }

    @Override
    public List<ReviewResponse> getAdminReviews(Enums.ReviewStatus status) {
        ensureAdminRole();
        List<Review> reviews = status == null
                ? reviewRepository.findAllByOrderByCreatedAtDesc()
                : reviewRepository.findByStatusOrderByCreatedAtDesc(status);
        return toResponses(reviews);
    }

    @Override
    @Transactional
    public ReviewResponse updateReviewStatus(Long id, ReviewStatusUpdateRequest request) {
        ensureAdminRole();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Review not found"));
        Integer nextScoreChange = request.getScoreChange() != null
                ? request.getScoreChange()
                : review.getScoreChange();

        if (request.getStatus() == Enums.ReviewStatus.PENALIZED && nextScoreChange >= 0) {
            throw new AppException(400, "Penalized reviews must have a negative score change");
        }

        boolean wasApplied = isScoreApplied(review.getStatus());
        boolean willBeApplied = isScoreApplied(request.getStatus());
        int oldScoreChange = review.getScoreChange() != null ? review.getScoreChange() : 0;
        int newScoreChange = nextScoreChange != null ? nextScoreChange : 0;
        int delta = calculateTrustScoreDelta(wasApplied, willBeApplied, oldScoreChange, newScoreChange);

        review.setStatus(request.getStatus());
        review.setScoreChange(nextScoreChange);
        review.setAiSuggestedPenalty(nextScoreChange != null && nextScoreChange < 0 ? nextScoreChange : null);

        Review savedReview = reviewRepository.save(review);
        if (delta != 0) {
            applyTrustScore(savedReview.getRevieweeId(), delta);
        }

        return toResponse(savedReview);
    }

    private MatchRequest findMatchRequest(Long id) {
        return matchRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Match request not found"));
    }

    private MatchPost findMatchPost(Long id) {
        return matchPostRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Match post not found"));
    }

    private Long resolveRevieweeId(MatchRequest matchRequest, MatchPost post, Long reviewerId) {
        if (matchRequest.getRequesterId() != null && matchRequest.getRequesterId().equals(reviewerId)) {
            return post.getUserId();
        }
        if (canManagePost(post, reviewerId)) {
            return matchRequest.getRequesterId();
        }

        throw new AppException(403, "Only match participants can review this match request");
    }

    private void ensureCanViewMatchRequestReviews(MatchRequest matchRequest, MatchPost post) {
        if (TokenUtils.hasRole("ADMIN")) {
            return;
        }

        Long currentUserId = TokenUtils.getCurrentUserId();
        if ((matchRequest.getRequesterId() != null && matchRequest.getRequesterId().equals(currentUserId))
                || canManagePost(post, currentUserId)) {
            return;
        }

        throw new AppException(403, "Access denied");
    }

    private boolean canManagePost(MatchPost post, Long userId) {
        if (post.getUserId() != null && post.getUserId().equals(userId)) {
            return true;
        }
        if (post.getTeamId() == null) {
            return false;
        }

        return teamRepository.findById(post.getTeamId())
                .map(Team::getCaptainId)
                .filter(captainId -> captainId.equals(userId))
                .isPresent();
    }

    private Enums.ReviewStatus resolveInitialStatus(Integer scoreChange) {
        if (scoreChange < PENDING_REVIEW_SCORE_THRESHOLD) {
            return Enums.ReviewStatus.PENDING_ADMIN_REVIEW;
        }

        return Enums.ReviewStatus.AUTO_PASSED;
    }

    private boolean isScoreApplied(Enums.ReviewStatus status) {
        return status == Enums.ReviewStatus.AUTO_PASSED || status == Enums.ReviewStatus.PENALIZED;
    }

    private int calculateTrustScoreDelta(boolean wasApplied,
                                         boolean willBeApplied,
                                         int oldScoreChange,
                                         int newScoreChange) {
        if (wasApplied && willBeApplied) {
            return newScoreChange - oldScoreChange;
        }
        if (!wasApplied && willBeApplied) {
            return newScoreChange;
        }
        if (wasApplied) {
            return -oldScoreChange;
        }

        return 0;
    }

    private void applyTrustScore(Long userId, Integer scoreChange) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(404, "Reviewee not found"));
        int currentTrustScore = user.getTrustScore() != null ? user.getTrustScore() : 100;
        user.setTrustScore(Math.max(0, currentTrustScore + scoreChange));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void ensureAdminRole() {
        if (!TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Only admins can manage reviews");
        }
    }

    private ReviewResponse toResponse(Review review) {
        Map<Long, User> users = mapById(userRepository.findAllById(collectUserIds(List.of(review))), User::getId);
        Map<Long, MatchRequest> matchRequests = mapById(
                matchRequestRepository.findAllById(nonNullList(review.getMatchRequestId())),
                MatchRequest::getId
        );
        return toResponse(review, users, matchRequests);
    }

    private List<ReviewResponse> toResponses(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }

        Map<Long, User> users = mapById(userRepository.findAllById(collectUserIds(reviews)), User::getId);
        Map<Long, MatchRequest> matchRequests = mapById(
                matchRequestRepository.findAllById(collectIds(reviews, Review::getMatchRequestId)),
                MatchRequest::getId
        );

        return reviews.stream()
                .map(review -> toResponse(review, users, matchRequests))
                .toList();
    }

    private ReviewResponse toResponse(Review review,
                                      Map<Long, User> users,
                                      Map<Long, MatchRequest> matchRequests) {
        User reviewer = users.get(review.getReviewerId());
        User reviewee = users.get(review.getRevieweeId());
        MatchRequest matchRequest = matchRequests.get(review.getMatchRequestId());

        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setReviewerId(review.getReviewerId());
        response.setReviewerName(reviewer != null ? reviewer.getFullName() : null);
        response.setRevieweeId(review.getRevieweeId());
        response.setRevieweeName(reviewee != null ? reviewee.getFullName() : null);
        response.setRevieweeTrustScore(reviewee != null ? reviewee.getTrustScore() : null);
        response.setMatchRequestId(review.getMatchRequestId());
        response.setMatchPostId(matchRequest != null ? matchRequest.getPostId() : null);
        response.setScoreChange(review.getScoreChange());
        response.setReason(review.getReason());
        response.setAiSuggestedPenalty(review.getAiSuggestedPenalty());
        response.setStatus(review.getStatus());
        response.setCreatedAt(review.getCreatedAt());
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

    private Set<Long> collectUserIds(Collection<Review> reviews) {
        return reviews.stream()
                .flatMap(review -> Stream.of(review.getReviewerId(), review.getRevieweeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<Long> collectIds(Collection<Review> reviews, Function<Review, Long> extractor) {
        return reviews.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String cleanRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(400, message);
        }

        return value.trim();
    }
}
