package com.example.backend.repository;

import com.example.backend.entity.MatchPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, JpaSpecificationExecutor<MatchPost> {
    List<MatchPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MatchPost> findByTeamIdInOrderByCreatedAtDesc(List<Long> teamIds);
}
