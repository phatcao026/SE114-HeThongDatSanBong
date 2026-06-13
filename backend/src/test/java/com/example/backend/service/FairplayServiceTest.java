package com.example.backend.service;

import com.example.backend.dto.request.FairplayDecisionRequest;
import com.example.backend.dto.request.OpponentReviewCreateRequest;
import com.example.backend.dto.response.OpponentReviewResponse;
import com.example.backend.entity.OpponentReview;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.OpponentReviewRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.FairplayServiceImpl;
import com.example.backend.utils.Enums;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FairplayServiceTest {

    @Mock
    private OpponentReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FairplayServiceImpl fairplayService;

    @Test
    public void testSubmitReview_Success() {
        Long reviewerId = 1L;
        OpponentReviewCreateRequest request = new OpponentReviewCreateRequest();
        request.setMatchId(10L);
        request.setRevieweeId(2L);
        request.setRatingType(Enums.OpponentRatingType.NO_SHOW);
        request.setComment("Did not show up");
        request.setImageUrl("http://image.com");

        when(userRepository.existsById(2L)).thenReturn(true);
        when(reviewRepository.existsByMatchIdAndReviewerId(10L, reviewerId)).thenReturn(false);

        fairplayService.submitReview(reviewerId, request);

        verify(reviewRepository, times(1)).save(any(OpponentReview.class));
    }

    @Test
    public void testSubmitReview_SelfReview_ThrowsBadRequest() {
        Long reviewerId = 1L;
        OpponentReviewCreateRequest request = new OpponentReviewCreateRequest();
        request.setMatchId(10L);
        request.setRevieweeId(1L); // Self
        request.setRatingType(Enums.OpponentRatingType.NO_SHOW);

        AppException exception = assertThrows(AppException.class, () -> {
            fairplayService.submitReview(reviewerId, request);
        });

        assertEquals(400, exception.getStatusCode());
        assertEquals("Bạn không thể tự đánh giá chính mình!", exception.getMessage());
    }

    @Test
    public void testSubmitReview_DuplicateReview_ThrowsBadRequest() {
        Long reviewerId = 1L;
        OpponentReviewCreateRequest request = new OpponentReviewCreateRequest();
        request.setMatchId(10L);
        request.setRevieweeId(2L);
        request.setRatingType(Enums.OpponentRatingType.NO_SHOW);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(reviewRepository.existsByMatchIdAndReviewerId(10L, reviewerId)).thenReturn(true); // Duplicate

        AppException exception = assertThrows(AppException.class, () -> {
            fairplayService.submitReview(reviewerId, request);
        });

        assertEquals(400, exception.getStatusCode());
        assertEquals("Bạn đã gửi đánh giá đối thủ cho trận đấu này rồi!", exception.getMessage());
    }

    @Test
    public void testGetPendingReviews_Success() {
        OpponentReview review = new OpponentReview();
        review.setId(100L);
        review.setMatchId(10L);
        review.setReviewerId(1L);
        review.setRevieweeId(2L);
        review.setRatingType(Enums.OpponentRatingType.BAD_BEHAVIOR);
        review.setComment("Aggressive");
        review.setStatus(Enums.FairplayStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.findByStatusOrderByCreatedAtDesc(Enums.FairplayStatus.PENDING))
                .thenReturn(List.of(review));

        User reviewer = new User();
        reviewer.setId(1L);
        reviewer.setFullName("User One");

        User reviewee = new User();
        reviewee.setId(2L);
        reviewee.setFullName("User Two");

        when(userRepository.findAllById(any())).thenReturn(List.of(reviewer, reviewee));

        List<OpponentReviewResponse> pending = fairplayService.getPendingReviews();

        assertFalse(pending.isEmpty());
        assertEquals(1, pending.size());
        assertEquals(100L, pending.get(0).getId());
        assertEquals("User One", pending.get(0).getReviewerName());
        assertEquals("User Two", pending.get(0).getRevieweeName());
    }

    @Test
    public void testResolveReview_Accepted_DecreasesReputation() {
        OpponentReview review = new OpponentReview();
        review.setId(100L);
        review.setReviewerId(1L);
        review.setRevieweeId(2L);
        review.setRatingType(Enums.OpponentRatingType.BAD_BEHAVIOR);
        review.setStatus(Enums.FairplayStatus.PENDING);

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));

        User reviewee = new User();
        reviewee.setId(2L);
        reviewee.setTrustScore(100);

        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewee));

        FairplayDecisionRequest decision = new FairplayDecisionRequest();
        decision.setIsAccepted(true);
        decision.setPointsApplied(-15);

        fairplayService.resolveReview(100L, decision);

        assertEquals(Enums.FairplayStatus.RESOLVED, review.getStatus());
        assertEquals(-15, review.getPointsApplied());
        assertEquals(85, reviewee.getTrustScore());
        verify(userRepository, times(1)).save(reviewee);
        verify(notificationService, times(1)).createNotification(
                eq(2L),
                anyString(),
                contains("bị trừ 15 điểm"),
                eq(Enums.NotificationType.SYSTEM)
        );
    }

    @Test
    public void testResolveReview_Rejected_Success() {
        OpponentReview review = new OpponentReview();
        review.setId(100L);
        review.setReviewerId(1L);
        review.setRevieweeId(2L);
        review.setStatus(Enums.FairplayStatus.PENDING);

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));

        FairplayDecisionRequest decision = new FairplayDecisionRequest();
        decision.setIsAccepted(false);

        fairplayService.resolveReview(100L, decision);

        assertEquals(Enums.FairplayStatus.REJECTED, review.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }
}
