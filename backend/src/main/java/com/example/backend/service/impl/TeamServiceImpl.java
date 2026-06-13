package com.example.backend.service.impl;

import com.example.backend.dto.request.TeamCreateRequest;
import com.example.backend.dto.request.TeamInvitationDecisionRequest;
import com.example.backend.dto.request.TeamInviteRequest;
import com.example.backend.dto.request.TeamUpdateRequest;
import com.example.backend.dto.response.TeamMemberResponse;
import com.example.backend.dto.response.TeamResponse;
import com.example.backend.entity.Team;
import com.example.backend.entity.TeamMember;
import com.example.backend.entity.User;
import com.example.backend.entity.Conversation;
import com.example.backend.entity.ConversationMember;
import com.example.backend.entity.ConversationMemberId;
import com.example.backend.exception.AppException;
import com.example.backend.repository.TeamMemberRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.ConversationMemberRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.service.TeamService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public TeamServiceImpl(TeamRepository teamRepository,
                           TeamMemberRepository teamMemberRepository,
                           UserRepository userRepository,
                           NotificationService notificationService,
                           ConversationRepository conversationRepository,
                           ConversationMemberRepository conversationMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
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
        Map<Long, TeamResponse> responsesByTeamId = new LinkedHashMap<>();

        teamRepository.findByCaptainIdOrderByCreatedAtDesc(currentUserId)
                .forEach(team -> responsesByTeamId.put(team.getId(),
                        toResponse(team, true, Enums.TeamMemberStatus.ACCEPTED)));

        teamMemberRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                        currentUserId,
                        Enums.TeamMemberStatus.ACCEPTED
                )
                .stream()
                .map(TeamMember::getTeam)
                .filter(team -> team != null && !responsesByTeamId.containsKey(team.getId()))
                .forEach(team -> responsesByTeamId.put(team.getId(),
                        toResponse(team, false, Enums.TeamMemberStatus.ACCEPTED)));

        return responsesByTeamId.values()
                .stream()
                .sorted(Comparator.comparing(TeamResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
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

        Conversation conversation = new Conversation();
        conversation.setType(Enums.ConversationType.TEAM);
        conversation.setName(cleanRequired(request.getName(), "Team name is required"));
        conversation.setCreatedAt(LocalDateTime.now());
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationMember captainMember = new ConversationMember();
        captainMember.setConversationId(savedConversation.getId());
        captainMember.setUserId(currentUserId);
        conversationMemberRepository.save(captainMember);

        Team team = new Team();
        team.setName(cleanRequired(request.getName(), "Team name is required"));
        team.setDescription(cleanOptional(request.getDescription()));
        team.setLevel(request.getLevel());
        team.setCaptainId(currentUserId);
        team.setConversationId(savedConversation.getId());
        team.setCreatedAt(LocalDateTime.now());

        Team savedTeam = teamRepository.save(team);
        ensureCaptainMembership(savedTeam);
        return toResponse(savedTeam, true, Enums.TeamMemberStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long id, TeamUpdateRequest request) {
        Team team = findTeam(id);
        ensureCanManageTeam(team);

        if (request.getName() != null) {
            String cleanName = cleanRequired(request.getName(), "Team name is required");
            team.setName(cleanName);
            if (team.getConversationId() != null) {
                conversationRepository.findById(team.getConversationId()).ifPresent(conv -> {
                    conv.setName(cleanName);
                    conversationRepository.save(conv);
                });
            }
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

        Long conversationId = team.getConversationId();
        TeamResponse response = toResponse(team);
        try {
            teamRepository.delete(team);
            teamRepository.flush();
            if (conversationId != null) {
                conversationRepository.deleteById(conversationId);
            }
            return response;
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(409, "Cannot delete team that is linked to another feature");
        }
    }

    @Override
    public List<TeamMemberResponse> getTeamMembers(Long id) {
        Team team = findTeam(id);
        return teamMemberRepository.findByTeamIdOrderByCreatedAtAsc(team.getId())
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public TeamMemberResponse inviteMember(Long id, TeamInviteRequest request) {
        Team team = findTeam(id);
        ensureCanManageTeam(team);

        User invitedUser = userRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new AppException(404, "User not found"));
        if (team.getCaptainId() != null && team.getCaptainId().equals(invitedUser.getId())) {
            throw new AppException(400, "Captain is already in this team");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(team.getId(), invitedUser.getId())
                .orElseGet(() -> buildTeamMember(team.getId(), invitedUser.getId(), Enums.TeamMemberStatus.PENDING));
        member.setTeam(team);
        member.setUser(invitedUser);

        if (member.getStatus() == Enums.TeamMemberStatus.ACCEPTED) {
            throw new AppException(409, "User is already a team member");
        }
        if (member.getStatus() == Enums.TeamMemberStatus.PENDING && member.getId() != null) {
            throw new AppException(409, "User already has a pending invitation");
        }

        member.setStatus(Enums.TeamMemberStatus.PENDING);
        if (member.getCreatedAt() == null) {
            member.setCreatedAt(LocalDateTime.now());
        }
        TeamMember savedMember = teamMemberRepository.save(member);
        notificationService.createNotification(
                invitedUser.getId(),
                "Team invitation",
                "You were invited to join team " + team.getName(),
                Enums.NotificationType.TEAM_INVITE
        );

        return toMemberResponse(savedMember);
    }

    @Override
    @Transactional
    public TeamMemberResponse removeMember(Long id, Long memberId) {
        Team team = findTeam(id);
        ensureCanManageTeam(team);

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(404, "Team member not found"));
        if (!team.getId().equals(member.getTeamId())) {
            throw new AppException(400, "Member does not belong to this team");
        }
        if (team.getCaptainId() != null && team.getCaptainId().equals(member.getUserId())) {
            throw new AppException(400, "Captain cannot be removed from the team");
        }

        TeamMemberResponse response = toMemberResponse(member);
        teamMemberRepository.delete(member);
        if (team.getConversationId() != null) {
            conversationMemberRepository.deleteById(new ConversationMemberId(team.getConversationId(), member.getUserId()));
        }
        return response;
    }

    @Override
    public List<TeamMemberResponse> getMyInvitations() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return teamMemberRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                        currentUserId,
                        Enums.TeamMemberStatus.PENDING
                )
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public TeamMemberResponse respondToInvitation(Long invitationId, TeamInvitationDecisionRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        TeamMember invitation = teamMemberRepository.findByIdAndUserId(invitationId, currentUserId)
                .orElseThrow(() -> new AppException(404, "Team invitation not found"));
        if (invitation.getStatus() != Enums.TeamMemberStatus.PENDING) {
            throw new AppException(400, "Only pending invitations can be answered");
        }

        Team team = invitation.getTeam() != null ? invitation.getTeam() : findTeam(invitation.getTeamId());
        boolean accepted = Boolean.TRUE.equals(request.getAccept());
        invitation.setStatus(accepted ? Enums.TeamMemberStatus.ACCEPTED : Enums.TeamMemberStatus.REJECTED);
        TeamMember savedInvitation = teamMemberRepository.save(invitation);

        if (accepted && team.getConversationId() != null) {
            if (!conversationMemberRepository.existsByConversationIdAndUserId(team.getConversationId(), currentUserId)) {
                ConversationMember member = new ConversationMember();
                member.setConversationId(team.getConversationId());
                member.setUserId(currentUserId);
                conversationMemberRepository.save(member);
            }
        }

        if (team.getCaptainId() != null) {
            String message = accepted
                    ? "A player accepted your invitation to join team " + team.getName()
                    : "A player rejected your invitation to join team " + team.getName();
            notificationService.createNotification(
                    team.getCaptainId(),
                    "Team invitation response",
                    message,
                    Enums.NotificationType.TEAM_INVITE
            );
        }

        return toMemberResponse(savedInvitation);
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Team not found"));
    }

    private Team findTeamWithCaptain(Long id) {
        return teamRepository.findByIdWithCaptain(id)
                .orElseThrow(() -> new AppException(404, "Team not found"));
    }

    private void ensureCaptainMembership(Team team) {
        if (team.getCaptainId() == null) {
            return;
        }

        teamMemberRepository.findByTeamIdAndUserId(team.getId(), team.getCaptainId())
                .orElseGet(() -> teamMemberRepository.save(
                        buildTeamMember(team.getId(), team.getCaptainId(), Enums.TeamMemberStatus.ACCEPTED)
                ));
    }

    private TeamMember buildTeamMember(Long teamId, Long userId, Enums.TeamMemberStatus status) {
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setStatus(status);
        member.setCreatedAt(LocalDateTime.now());
        return member;
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
        return toResponse(team, null, null);
    }

    private TeamResponse toResponse(Team team, Boolean isCaptain, Enums.TeamMemberStatus memberStatus) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setName(team.getName());
        response.setDescription(team.getDescription());
        response.setCaptainId(team.getCaptainId());
        response.setLevel(team.getLevel());
        response.setCreatedAt(team.getCreatedAt());
        response.setIsCaptain(isCaptain);
        response.setMemberStatus(memberStatus);
        response.setConversationId(team.getConversationId());

        User captain = team.getCaptain();
        if (captain != null) {
            response.setCaptainName(captain.getFullName());
        }

        return response;
    }

    private TeamMemberResponse toMemberResponse(TeamMember member) {
        Team team = member.getTeam() != null
                ? member.getTeam()
                : teamRepository.findById(member.getTeamId()).orElse(null);
        User user = member.getUser() != null
                ? member.getUser()
                : userRepository.findById(member.getUserId()).orElse(null);

        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(member.getId());
        response.setTeamId(member.getTeamId());
        response.setTeamName(team != null ? team.getName() : null);
        response.setUserId(member.getUserId());
        response.setUserName(user != null ? user.getFullName() : null);
        response.setUserEmail(user != null ? user.getEmail() : null);
        response.setStatus(member.getStatus());
        response.setCaptain(team != null && team.getCaptainId() != null
                && team.getCaptainId().equals(member.getUserId()));
        response.setCreatedAt(member.getCreatedAt());
        return response;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new AppException(400, "Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
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
