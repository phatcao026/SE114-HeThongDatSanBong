package com.example.backend.repository;

import com.example.backend.entity.MatchRequest;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    List<MatchRequest> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<MatchRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<MatchRequest> findByPostIdAndStatus(Long postId, Enums.RequestStatus status);

    Optional<MatchRequest> findFirstByPostIdAndStatus(Long postId, Enums.RequestStatus status);

    boolean existsByPostIdAndRequesterId(Long postId, Long requesterId);

    long countByPostId(Long postId);

    long countByPostIdIn(Collection<Long> postIds);
}
