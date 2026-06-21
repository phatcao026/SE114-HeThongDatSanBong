package com.example.backend.repository;

import com.example.backend.entity.OpponentReview;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpponentReviewRepository extends JpaRepository<OpponentReview, Long> {
    List<OpponentReview> findByStatusOrderByCreatedAtDesc(Enums.FairplayStatus status);
    List<OpponentReview> findByStatusInOrderByCreatedAtDesc(List<Enums.FairplayStatus> statuses);
    List<OpponentReview> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);
    List<OpponentReview> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);
    boolean existsByMatchIdAndReviewerId(Long matchId, Long reviewerId);
    long countByStatus(Enums.FairplayStatus status);

    @Query("SELECT r.matchId FROM OpponentReview r WHERE r.reviewerId = :reviewerId")
    List<Long> findMatchIdsByReviewerId(@Param("reviewerId") Long reviewerId);
}
