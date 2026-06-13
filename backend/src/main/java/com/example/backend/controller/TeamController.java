package com.example.backend.controller;

import com.example.backend.dto.request.TeamCreateRequest;
import com.example.backend.dto.request.TeamInvitationDecisionRequest;
import com.example.backend.dto.request.TeamInviteRequest;
import com.example.backend.dto.request.TeamUpdateRequest;
import com.example.backend.dto.response.TeamMemberResponse;
import com.example.backend.dto.response.TeamResponse;
import com.example.backend.service.TeamService;
import com.example.backend.utils.Enums;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams(
            @RequestParam(required = false) Enums.TeamLevel level,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(teamService.getTeams(level, keyword));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TeamResponse>> getMyTeams() {
        return ResponseEntity.ok(teamService.getMyTeams());
    }

    @GetMapping("/invitations/my")
    public ResponseEntity<List<TeamMemberResponse>> getMyInvitations() {
        return ResponseEntity.ok(teamService.getMyInvitations());
    }

    @PutMapping("/invitations/{invitationId}")
    public ResponseEntity<TeamMemberResponse> respondToInvitation(
            @PathVariable Long invitationId,
            @Valid @RequestBody TeamInvitationDecisionRequest request) {
        return ResponseEntity.ok(teamService.respondToInvitation(invitationId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long id,
                                                   @RequestBody TeamUpdateRequest request) {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TeamResponse> deleteTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.deleteTeam(id));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamMembers(id));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<TeamMemberResponse> inviteMember(@PathVariable Long id,
                                                           @Valid @RequestBody TeamInviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.inviteMember(id, request));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<TeamMemberResponse> removeMember(@PathVariable Long id,
                                                           @PathVariable Long memberId) {
        return ResponseEntity.ok(teamService.removeMember(id, memberId));
    }
}
