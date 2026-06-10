package com.example.backend.service;

import com.example.backend.dto.request.MatchPostCreateRequest;
import com.example.backend.dto.request.MatchPostUpdateRequest;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.utils.Enums;

import java.time.LocalDate;
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
}
