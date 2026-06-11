package com.example.backend.repository;

import com.example.backend.entity.MatchPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, JpaSpecificationExecutor<MatchPost> {
    List<MatchPost> findAllByOrderByCreatedAtDesc();

    List<MatchPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MatchPost> findByTeamIdInOrderByCreatedAtDesc(List<Long> teamIds);

    List<MatchPost> findByStatusOrderByCreatedAtDesc(com.example.backend.utils.Enums.PostStatus status);

    long countByStatus(com.example.backend.utils.Enums.PostStatus status);
}
