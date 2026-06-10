package com.example.backend.service;

import com.example.backend.dto.request.MatchRequestCreateRequest;
import com.example.backend.dto.request.MatchRequestStatusUpdateRequest;
import com.example.backend.dto.response.MatchRequestResponse;

import java.util.List;

public interface MatchRequestService {
    List<MatchRequestResponse> getRequestsByPost(Long postId);

    List<MatchRequestResponse> getMyRequests();

    MatchRequestResponse createRequest(Long postId, MatchRequestCreateRequest request);

    MatchRequestResponse updateRequestStatus(Long requestId, MatchRequestStatusUpdateRequest request);
}
