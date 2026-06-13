package com.example.backend.repository;

import com.example.backend.entity.TeamMember;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    @EntityGraph(attributePaths = {"user", "team", "team.captain"})
    List<TeamMember> findByTeamIdOrderByCreatedAtAsc(Long teamId);

    @EntityGraph(attributePaths = {"team", "team.captain", "user"})
    List<TeamMember> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Enums.TeamMemberStatus status);

    @EntityGraph(attributePaths = {"team", "team.captain", "user"})
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    @EntityGraph(attributePaths = {"team", "team.captain", "user"})
    Optional<TeamMember> findByIdAndUserId(Long id, Long userId);
}
