package com.example.backend.service.impl;

import com.example.backend.dto.request.TeamCreateRequest;
import com.example.backend.dto.request.TeamUpdateRequest;
import com.example.backend.dto.response.TeamResponse;
import com.example.backend.entity.Team;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TeamService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamServiceImpl(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TeamResponse> getTeams(Enums.TeamLevel level, String keyword) {
        String cleanKeyword = cleanOptional(keyword);
        return findTeams(level, cleanKeyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TeamResponse> getMyTeams() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return teamRepository.findByCaptainIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TeamResponse getTeamById(Long id) {
        return toResponse(findTeamWithCaptain(id));
    }

    @Override
    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        Team team = new Team();
        team.setName(cleanRequired(request.getName(), "Team name is required"));
        team.setDescription(cleanOptional(request.getDescription()));
        team.setLevel(request.getLevel());
        team.setCaptainId(currentUserId);
        team.setCreatedAt(LocalDateTime.now());

        return toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long id, TeamUpdateRequest request) {
        Team team = findTeam(id);
        ensureCanManageTeam(team);

        if (request.getName() != null) {
            team.setName(cleanRequired(request.getName(), "Team name is required"));
        }
        if (request.getDescription() != null) {
            team.setDescription(cleanOptional(request.getDescription()));
        }
        if (request.getLevel() != null) {
            team.setLevel(request.getLevel());
        }

        return toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse deleteTeam(Long id) {
        Team team = findTeam(id);
        ensureCanManageTeam(team);

        TeamResponse response = toResponse(team);
        try {
            teamRepository.delete(team);
            teamRepository.flush();
            return response;
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(409, "Cannot delete team that is linked to another feature");
        }
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Team not found"));
    }

    private Team findTeamWithCaptain(Long id) {
        return teamRepository.findByIdWithCaptain(id)
                .orElseThrow(() -> new AppException(404, "Team not found"));
    }

    private List<Team> findTeams(Enums.TeamLevel level, String keyword) {
        if (level != null && keyword != null) {
            return teamRepository.findByLevelAndNameContainingIgnoreCaseOrderByCreatedAtDesc(level, keyword);
        }
        if (level != null) {
            return teamRepository.findByLevelOrderByCreatedAtDesc(level);
        }
        if (keyword != null) {
            return teamRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
        }

        return teamRepository.findByOrderByCreatedAtDesc();
    }

    private void ensureCanManageTeam(Team team) {
        if (TokenUtils.hasRole("ADMIN")) {
            return;
        }

        Long currentUserId = TokenUtils.getCurrentUserId();
        if (team.getCaptainId() == null || !team.getCaptainId().equals(currentUserId)) {
            throw new AppException(403, "Only the captain can manage this team");
        }
    }

    private TeamResponse toResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());
        response.setDescription(team.getDescription());
        response.setCaptainId(team.getCaptainId());
        response.setLevel(team.getLevel());
        response.setCreatedAt(team.getCreatedAt());

        User captain = team.getCaptain();
        if (captain != null) {
            response.setCaptainName(captain.getFullName());
        }

        return response;
    }

    private String cleanRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(400, message);
        }

        return value.trim();
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
