package com.example.backend.repository;

import com.example.backend.entity.Team;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    @EntityGraph(attributePaths = {"captain"})
    List<Team> findByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"captain"})
    List<Team> findByLevelOrderByCreatedAtDesc(Enums.TeamLevel level);

    @EntityGraph(attributePaths = {"captain"})
    List<Team> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    @EntityGraph(attributePaths = {"captain"})
    List<Team> findByLevelAndNameContainingIgnoreCaseOrderByCreatedAtDesc(Enums.TeamLevel level, String keyword);

    @EntityGraph(attributePaths = {"captain"})
    List<Team> findByCaptainIdOrderByCreatedAtDesc(Long captainId);

    @EntityGraph(attributePaths = {"captain"})
    @Query("SELECT t FROM Team t WHERE t.id = :id")
    Optional<Team> findByIdWithCaptain(@Param("id") Long id);
}
