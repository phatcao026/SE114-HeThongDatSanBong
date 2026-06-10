package com.example.backend.service;

import com.example.backend.dto.request.TeamCreateRequest;
import com.example.backend.dto.request.TeamUpdateRequest;
import com.example.backend.dto.response.TeamResponse;
import com.example.backend.utils.Enums;

import java.util.List;

public interface TeamService {
    List<TeamResponse> getTeams(Enums.TeamLevel level, String keyword);

    List<TeamResponse> getMyTeams();

    TeamResponse getTeamById(Long id);

    TeamResponse createTeam(TeamCreateRequest request);

    TeamResponse updateTeam(Long id, TeamUpdateRequest request);

    TeamResponse deleteTeam(Long id);
}
