package com.example.backend.service;

import com.example.backend.dto.request.MatchPostCreateRequest;
import com.example.backend.dto.request.MatchPostUpdateRequest;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.dto.response.RecommendedMatchResponse;
import com.example.backend.utils.Enums;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MatchPostService {
    List<MatchPostResponse> getMatchPosts(Enums.PostStatus status,
                                          Enums.PostType postType,
                                          Enums.TeamLevel skillLevel,
                                          Long fieldId,
                                          LocalDate date);

    List<MatchPostResponse> getMyMatchPosts();

    MatchPostResponse getMatchPostById(Long id);

    MatchPostResponse createMatchPost(MatchPostCreateRequest request);

    MatchPostResponse updateMatchPost(Long id, MatchPostUpdateRequest request);

    MatchPostResponse closeMatchPost(Long id);

    List<RecommendedMatchResponse> getSmartRecommendations(
            String playstyleNote,
            LocalDate date,
            LocalTime timeStart,
            LocalTime timeEnd,
            Enums.TeamLevel skillLevel,
            Boolean hasField,
            Enums.PostType postType,
            String position
    );
}
