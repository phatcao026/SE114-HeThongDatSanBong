package com.example.backend.service.impl;

import com.example.backend.dto.request.FairplayDecisionRequest;
import com.example.backend.dto.request.OpponentReviewCreateRequest;
import com.example.backend.dto.response.OpponentReviewResponse;
import com.example.backend.entity.OpponentReview;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.OpponentReviewRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.FairplayService;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.Enums;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FairplayServiceImpl implements FairplayService {

    private final OpponentReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FairplayServiceImpl(OpponentReviewRepository reviewRepository,
                                UserRepository userRepository,
                                NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void submitReview(Long reviewerId, OpponentReviewCreateRequest request) {
        if (reviewerId.equals(request.getRevieweeId())) {
            throw new AppException(400, "Bạn không thể tự đánh giá chính mình!");
        }

        if (!userRepository.existsById(request.getRevieweeId())) {
            throw new AppException(404, "Không tìm thấy đối thủ bị báo cáo!");
        }

        if (reviewRepository.existsByMatchIdAndReviewerId(request.getMatchId(), reviewerId)) {
            throw new AppException(400, "Bạn đã gửi đánh giá đối thủ cho trận đấu này rồi!");
        }

        OpponentReview review = new OpponentReview();
        review.setMatchId(request.getMatchId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(request.getRevieweeId());
        review.setRatingType(request.getRatingType());
        review.setComment(request.getComment() != null ? request.getComment().trim() : null);
        review.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        review.setStatus(Enums.FairplayStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpponentReviewResponse> getPendingReviews() {
        List<OpponentReview> reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(Enums.FairplayStatus.PENDING);
        if (reviews.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = new HashSet<>();
        for (OpponentReview r : reviews) {
            userIds.add(r.getReviewerId());
            userIds.add(r.getRevieweeId());
        }

        Map<Long, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return reviews.stream()
                .map(r -> toResponse(r, usersMap.get(r.getReviewerId()), usersMap.get(r.getRevieweeId())))
                .toList();
    }

    @Override
    @Transactional
    public void resolveReview(Long reviewId, FairplayDecisionRequest request) {
        OpponentReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy báo cáo"));

        if (review.getStatus() != Enums.FairplayStatus.PENDING) {
            throw new AppException(400, "Báo cáo này đã được xử lý rồi");
        }

        if (Boolean.TRUE.equals(request.getIsAccepted())) {
            review.setStatus(Enums.FairplayStatus.RESOLVED);
            review.setPointsApplied(request.getPointsApplied() != null ? request.getPointsApplied() : 0);

            User reviewee = userRepository.findById(review.getRevieweeId())
                    .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng bị báo cáo"));

            int currentScore = reviewee.getTrustScore() != null ? reviewee.getTrustScore() : 100;
            reviewee.setTrustScore(Math.max(0, currentScore + review.getPointsApplied()));
            reviewee.setUpdatedAt(LocalDateTime.now());
            userRepository.save(reviewee);

            // Gửi thông báo cho người chơi bị tố cáo
            try {
                String changeText = review.getPointsApplied() >= 0
                        ? ("được cộng " + review.getPointsApplied() + " điểm")
                        : ("bị trừ " + Math.abs(review.getPointsApplied()) + " điểm");

                String reasonText = "";
                if (review.getRatingType() == Enums.OpponentRatingType.NO_SHOW) {
                    reasonText = " do bùng kèo/hủy phút chót";
                } else if (review.getRatingType() == Enums.OpponentRatingType.BAD_BEHAVIOR) {
                    reasonText = " do hành vi chơi bạo lực/gây rối";
                } else if (review.getRatingType() == Enums.OpponentRatingType.GOOD) {
                    reasonText = " vì thi đấu đẹp/thân thiện";
                }

                String title = "Phán quyết từ Tòa án Fairplay";
                String content = "Theo phán quyết của Tòa án Fairplay, bạn " + changeText + " uy tín" + reasonText
                        + ". Điểm uy tín hiện tại của bạn là: " + reviewee.getTrustScore() + "đ.";

                notificationService.createNotification(reviewee.getId(), title, content, Enums.NotificationType.SYSTEM);
            } catch (Exception e) {
                System.err.println("Lỗi gửi thông báo phán quyết Fairplay: " + e.getMessage());
            }
        } else {
            review.setStatus(Enums.FairplayStatus.REJECTED);
        }

        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMySubmittedMatchIds(Long reviewerId) {
        return reviewRepository.findMatchIdsByReviewerId(reviewerId);
    }

    private OpponentReviewResponse toResponse(OpponentReview r, User reviewer, User reviewee) {
        OpponentReviewResponse res = new OpponentReviewResponse();
        res.setId(r.getId());
        res.setMatchId(r.getMatchId());
        res.setReviewerId(r.getReviewerId());
        res.setReviewerName(reviewer != null ? reviewer.getFullName() : null);
        res.setRevieweeId(r.getRevieweeId());
        res.setRevieweeName(reviewee != null ? reviewee.getFullName() : null);
        res.setRatingType(r.getRatingType());
        res.setComment(r.getComment());
        res.setStatus(r.getStatus());
        res.setPointsApplied(r.getPointsApplied());
        res.setImageUrl(r.getImageUrl());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
