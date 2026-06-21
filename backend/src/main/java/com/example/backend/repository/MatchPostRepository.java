package com.example.backend.repository;

import com.example.backend.entity.MatchPost;
import com.example.backend.utils.Enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchPostRepository extends JpaRepository<MatchPost, Long>, JpaSpecificationExecutor<MatchPost> {
    List<MatchPost> findAllByOrderByCreatedAtDesc();

    List<MatchPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MatchPost> findByStatusOrderByCreatedAtDesc(com.example.backend.utils.Enums.PostStatus status);

    long countByStatus(com.example.backend.utils.Enums.PostStatus status);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT m FROM MatchPost m WHERE m.status = 'OPEN' " +
           "AND m.postType = :postType " +
           "AND m.userId != :currentUserId " +
           "AND (:date IS NULL OR m.date = :date) " +
           "AND (:skillLevel IS NULL OR m.skillLevel = :skillLevel) " +
           "AND (:hasField IS NULL OR m.hasField = :hasField) " +
           "ORDER BY m.createdAt DESC")
    Page<MatchPost> findPotentialMatches(
            @Param("currentUserId") Long currentUserId,
            @Param("postType") Enums.PostType postType,
            @Param("date") java.time.LocalDate date,
            @Param("skillLevel") Enums.TeamLevel skillLevel,
            @Param("hasField") Boolean hasField,
            Pageable pageable
    );
}
