package com.example.backend.repository;

import com.example.backend.entity.Review;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    List<Review> findByMatchRequestIdOrderByCreatedAtDesc(Long matchRequestId);

    List<Review> findByStatusOrderByCreatedAtDesc(Enums.ReviewStatus status);

    boolean existsByMatchRequestIdAndReviewerId(Long matchRequestId, Long reviewerId);
}
