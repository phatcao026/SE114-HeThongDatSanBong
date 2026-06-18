package com.example.backend.controller;

import com.example.backend.dto.request.MatchPostCreateRequest;
import com.example.backend.dto.request.MatchPostUpdateRequest;
import com.example.backend.dto.request.MatchRequestCreateRequest;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.dto.response.MatchRequestResponse;
import com.example.backend.dto.response.RecommendedMatchResponse;
import com.example.backend.service.MatchPostService;
import com.example.backend.service.MatchRequestService;
import com.example.backend.utils.Enums;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/match-posts")
public class MatchPostController {
    private final MatchPostService matchPostService;
    private final MatchRequestService matchRequestService;

    public MatchPostController(MatchPostService matchPostService,
                               MatchRequestService matchRequestService) {
        this.matchPostService = matchPostService;
        this.matchRequestService = matchRequestService;
    }

    @GetMapping
    public ResponseEntity<List<MatchPostResponse>> getMatchPosts(
            @RequestParam(required = false) Enums.PostStatus status,
            @RequestParam(required = false) Enums.PostType postType,
            @RequestParam(required = false) Enums.TeamLevel skillLevel,
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(matchPostService.getMatchPosts(status, postType, skillLevel, fieldId, date));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendedMatchResponse>> getSmartRecommendations(
            @RequestParam(required = false) String playstyleNote,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeEnd,
            @RequestParam(required = false) Enums.TeamLevel skillLevel,
            @RequestParam(required = false) Boolean hasField,
            @RequestParam(required = false) Enums.PostType postType,
            @RequestParam(required = false) String position) {
        return ResponseEntity.ok(matchPostService.getSmartRecommendations(
                playstyleNote, date, timeStart, timeEnd, skillLevel, hasField, postType, position));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MatchPostResponse>> getMyMatchPosts() {
        return ResponseEntity.ok(matchPostService.getMyMatchPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchPostResponse> getMatchPostById(@PathVariable Long id) {
        return ResponseEntity.ok(matchPostService.getMatchPostById(id));
    }

    @PostMapping
    public ResponseEntity<MatchPostResponse> createMatchPost(@RequestBody MatchPostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchPostService.createMatchPost(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchPostResponse> updateMatchPost(@PathVariable Long id,
                                                             @RequestBody MatchPostUpdateRequest request) {
        return ResponseEntity.ok(matchPostService.updateMatchPost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MatchPostResponse> closeMatchPost(@PathVariable Long id) {
        return ResponseEntity.ok(matchPostService.closeMatchPost(id));
    }

    @GetMapping("/{id}/requests")
    public ResponseEntity<List<MatchRequestResponse>> getRequestsByPost(@PathVariable Long id) {
        return ResponseEntity.ok(matchRequestService.getRequestsByPost(id));
    }

    @PostMapping("/{id}/requests")
    public ResponseEntity<MatchRequestResponse> createRequest(@PathVariable Long id,
                                                              @RequestBody MatchRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchRequestService.createRequest(id, request));
    }
}
