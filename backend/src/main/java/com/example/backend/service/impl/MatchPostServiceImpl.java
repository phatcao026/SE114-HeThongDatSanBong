package com.example.backend.service.impl;

import com.example.backend.dto.request.MatchPostCreateRequest;
import com.example.backend.dto.request.MatchPostUpdateRequest;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.Team;
import com.example.backend.entity.TimeSlot;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.repository.TimeSlotRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.dto.ai.AiOpponentDto;
import com.example.backend.dto.ai.AiRecommendationResult;
import com.example.backend.dto.response.RecommendedMatchResponse;
import com.example.backend.service.MatchPostService;
import com.example.backend.service.ai.GroqAiService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MatchPostServiceImpl implements MatchPostService {
    private static final List<Enums.BookingStatus> VALID_POST_BOOKING_STATUSES = List.of(
            Enums.BookingStatus.DEPOSIT_PAID,
            Enums.BookingStatus.CONFIRMED
    );

    private final MatchPostRepository matchPostRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final GroqAiService groqAiService;

    public MatchPostServiceImpl(MatchPostRepository matchPostRepository,
                                MatchRequestRepository matchRequestRepository,
                                BookingRepository bookingRepository,
                                FieldRepository fieldRepository,
                                TimeSlotRepository timeSlotRepository,
                                TeamRepository teamRepository,
                                UserRepository userRepository,
                                GroqAiService groqAiService) {
        this.matchPostRepository = matchPostRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.groqAiService = groqAiService;
    }

    @Override
    public List<MatchPostResponse> getMatchPosts(Enums.PostStatus status,
                                                 Enums.PostType postType,
                                                 Enums.TeamLevel skillLevel,
                                                 Long fieldId,
                                                 LocalDate date) {
        Enums.PostStatus effectiveStatus = status != null ? status : Enums.PostStatus.OPEN;
        Specification<MatchPost> spec = byFilters(effectiveStatus, postType, skillLevel, fieldId, date);
        List<MatchPost> posts = matchPostRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toResponses(posts);
    }

    @Override
    public List<MatchPostResponse> getMyMatchPosts() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        return toResponses(matchPostRepository.findByUserIdOrderByCreatedAtDesc(currentUserId));
    }

    @Override
    public MatchPostResponse getMatchPostById(Long id) {
        return toResponse(findPost(id));
    }

    @Override
    @Transactional
    public MatchPostResponse createMatchPost(MatchPostCreateRequest request) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        if (request.getPostType() == null) {
            throw new AppException(400, "Post type is required");
        }
        if (request.getSkillLevel() == null) {
            throw new AppException(400, "Skill level is required");
        }

        MatchPost post = new MatchPost();
        post.setUserId(currentUserId);
        post.setPostType(request.getPostType());
        post.setSkillLevel(request.getSkillLevel());
        post.setCostSharing(cleanOptional(request.getCostSharing()));
        post.setMessage(cleanOptional(request.getMessage()));
        post.setStatus(Enums.PostStatus.OPEN);
        post.setCreatedAt(LocalDateTime.now());

        applyTeam(post, request.getTeamId(), currentUserId);
        applyBookingOrManualSchedule(
                post,
                request.getBookingId(),
                request.getFieldId(),
                request.getDate(),
                request.getTimeStart(),
                request.getTimeEnd(),
                currentUserId,
                true
        );

        return toResponse(matchPostRepository.save(post));
    }

    @Override
    @Transactional
    public MatchPostResponse updateMatchPost(Long id, MatchPostUpdateRequest request) {
        MatchPost post = findPost(id);
        ensureCanManagePost(post);

        if (post.getStatus() == Enums.PostStatus.MATCHED && request.getStatus() != Enums.PostStatus.CLOSED) {
            throw new AppException(400, "Matched post cannot be edited");
        }
        if (post.getStatus() != Enums.PostStatus.OPEN && hasContentUpdate(request)) {
            throw new AppException(400, "Only open posts can be edited");
        }

        Long currentUserId = TokenUtils.getCurrentUserId();
        if (request.getTeamId() != null) {
            applyTeam(post, request.getTeamId(), currentUserId);
        }
        if (request.getBookingId() != null) {
            applyBookingOrManualSchedule(
                    post,
                    request.getBookingId(),
                    request.getFieldId(),
                    request.getDate(),
                    request.getTimeStart(),
                    request.getTimeEnd(),
                    currentUserId,
                    false
            );
        } else {
            applyManualUpdates(post, request);
        }
        if (request.getPostType() != null) {
            post.setPostType(request.getPostType());
        }
        if (request.getSkillLevel() != null) {
            post.setSkillLevel(request.getSkillLevel());
        }
        if (request.getCostSharing() != null) {
            post.setCostSharing(cleanOptional(request.getCostSharing()));
        }
        if (request.getMessage() != null) {
            post.setMessage(cleanOptional(request.getMessage()));
        }
        if (request.getStatus() != null) {
            if (request.getStatus() == Enums.PostStatus.MATCHED) {
                throw new AppException(400, "Match status must be changed by accepting a request");
            }
            post.setStatus(request.getStatus());
        }

        validateSchedule(post, false);
        return toResponse(matchPostRepository.save(post));
    }

    @Override
    @Transactional
    public MatchPostResponse closeMatchPost(Long id) {
        MatchPost post = findPost(id);
        ensureCanManagePost(post);

        post.setStatus(Enums.PostStatus.CLOSED);
        return toResponse(matchPostRepository.save(post));
    }

    private Specification<MatchPost> byFilters(Enums.PostStatus status,
                                               Enums.PostType postType,
                                               Enums.TeamLevel skillLevel,
                                               Long fieldId,
                                               LocalDate date) {
        Specification<MatchPost> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (status != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
        }
        if (postType != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("postType"), postType));
        }
        if (skillLevel != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("skillLevel"), skillLevel));
        }
        if (fieldId != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("fieldId"), fieldId));
        }
        if (date != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("date"), date));
        }

        return spec;
    }

    private void applyTeam(MatchPost post, Long teamId, Long currentUserId) {
        if (teamId == null) {
            return;
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(404, "Team not found"));
        if (!TokenUtils.hasRole("ADMIN") && (team.getCaptainId() == null || !team.getCaptainId().equals(currentUserId))) {
            throw new AppException(403, "Only the team captain can post for this team");
        }

        post.setTeamId(team.getId());
    }

    private void applyBookingOrManualSchedule(MatchPost post,
                                              Long bookingId,
                                              Long fieldId,
                                              LocalDate date,
                                              LocalTime timeStart,
                                              LocalTime timeEnd,
                                              Long currentUserId,
                                              boolean required) {
        if (bookingId != null) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new AppException(404, "Booking not found"));
            if (!TokenUtils.hasRole("ADMIN") && !booking.getUserId().equals(currentUserId)) {
                throw new AppException(403, "Access denied");
            }
            if (!VALID_POST_BOOKING_STATUSES.contains(booking.getStatus())) {
                throw new AppException(400, "Booking must be deposit-paid or confirmed before creating a match post");
            }

            TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                    .orElseThrow(() -> new AppException(404, "Time slot not found"));
            validateBookingOverride(fieldId, date, timeStart, timeEnd, booking, timeSlot);

            post.setBookingId(booking.getId());
            post.setFieldId(booking.getFieldId());
            post.setDate(booking.getBookingDate());
            post.setTimeStart(timeSlot.getStartTime());
            post.setTimeEnd(timeSlot.getEndTime());
            validateSchedule(post, true);
            return;
        }

        if (fieldId != null) {
            fieldRepository.findById(fieldId)
                    .orElseThrow(() -> new AppException(404, "Field not found"));
            post.setFieldId(fieldId);
        }
        post.setDate(date);
        post.setTimeStart(timeStart);
        post.setTimeEnd(timeEnd);
        validateSchedule(post, required);
    }

    private void applyManualUpdates(MatchPost post, MatchPostUpdateRequest request) {
        if (request.getFieldId() != null) {
            fieldRepository.findById(request.getFieldId())
                    .orElseThrow(() -> new AppException(404, "Field not found"));
            post.setFieldId(request.getFieldId());
            post.setBookingId(null);
        }
        if (request.getDate() != null) {
            post.setDate(request.getDate());
            post.setBookingId(null);
        }
        if (request.getTimeStart() != null) {
            post.setTimeStart(request.getTimeStart());
            post.setBookingId(null);
        }
        if (request.getTimeEnd() != null) {
            post.setTimeEnd(request.getTimeEnd());
            post.setBookingId(null);
        }
    }

    private void validateBookingOverride(Long fieldId,
                                         LocalDate date,
                                         LocalTime timeStart,
                                         LocalTime timeEnd,
                                         Booking booking,
                                         TimeSlot timeSlot) {
        if (fieldId != null && !fieldId.equals(booking.getFieldId())) {
            throw new AppException(400, "Field does not match booking");
        }
        if (date != null && !date.equals(booking.getBookingDate())) {
            throw new AppException(400, "Date does not match booking");
        }
        if (timeStart != null && !timeStart.equals(timeSlot.getStartTime())) {
            throw new AppException(400, "Start time does not match booking time slot");
        }
        if (timeEnd != null && !timeEnd.equals(timeSlot.getEndTime())) {
            throw new AppException(400, "End time does not match booking time slot");
        }
    }

    private void validateSchedule(MatchPost post, boolean required) {
        if (required && post.getDate() == null) {
            throw new AppException(400, "Match date is required");
        }
        if (required && post.getTimeStart() == null) {
            throw new AppException(400, "Start time is required");
        }
        if (required && post.getTimeEnd() == null) {
            throw new AppException(400, "End time is required");
        }
        if (post.getDate() != null && post.getDate().isBefore(LocalDate.now())) {
            throw new AppException(400, "Match date must be today or in the future");
        }
        if (post.getTimeStart() != null && post.getTimeEnd() != null
                && !post.getTimeStart().isBefore(post.getTimeEnd())) {
            throw new AppException(400, "Start time must be before end time");
        }
    }

    private boolean hasContentUpdate(MatchPostUpdateRequest request) {
        return request.getTeamId() != null
                || request.getFieldId() != null
                || request.getBookingId() != null
                || request.getDate() != null
                || request.getTimeStart() != null
                || request.getTimeEnd() != null
                || request.getPostType() != null
                || request.getSkillLevel() != null
                || request.getCostSharing() != null
                || request.getMessage() != null;
    }

    private MatchPost findPost(Long id) {
        return matchPostRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Match post not found"));
    }

    private void ensureCanManagePost(MatchPost post) {
        if (TokenUtils.hasRole("ADMIN")) {
            return;
        }

        Long currentUserId = TokenUtils.getCurrentUserId();
        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            return;
        }
        if (post.getTeamId() != null) {
            Team team = teamRepository.findById(post.getTeamId())
                    .orElseThrow(() -> new AppException(404, "Team not found"));
            if (team.getCaptainId() != null && team.getCaptainId().equals(currentUserId)) {
                return;
            }
        }

        throw new AppException(403, "Only the post owner can manage this match post");
    }

    private MatchPostResponse toResponse(MatchPost post) {
        Map<Long, User> users = mapById(userRepository.findAllById(nonNullList(post.getUserId())), User::getId);
        Map<Long, Team> teams = mapById(teamRepository.findAllById(nonNullList(post.getTeamId())), Team::getId);
        Map<Long, Field> fields = mapById(fieldRepository.findAllById(nonNullList(post.getFieldId())), Field::getId);
        return toResponse(post, users, teams, fields);
    }

    private List<MatchPostResponse> toResponses(List<MatchPost> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        Map<Long, User> users = mapById(userRepository.findAllById(collectIds(posts, MatchPost::getUserId)), User::getId);
        Map<Long, Team> teams = mapById(teamRepository.findAllById(collectIds(posts, MatchPost::getTeamId)), Team::getId);
        Map<Long, Field> fields = mapById(fieldRepository.findAllById(collectIds(posts, MatchPost::getFieldId)), Field::getId);

        return posts.stream()
                .map(post -> toResponse(post, users, teams, fields))
                .toList();
    }

    private MatchPostResponse toResponse(MatchPost post,
                                         Map<Long, User> users,
                                         Map<Long, Team> teams,
                                         Map<Long, Field> fields) {
        MatchPostResponse response = new MatchPostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setUserName(users.containsKey(post.getUserId()) ? users.get(post.getUserId()).getFullName() : null);
        response.setTeamId(post.getTeamId());
        response.setTeamName(teams.containsKey(post.getTeamId()) ? teams.get(post.getTeamId()).getName() : null);
        response.setFieldId(post.getFieldId());
        response.setFieldName(fields.containsKey(post.getFieldId()) ? fields.get(post.getFieldId()).getName() : null);
        response.setBookingId(post.getBookingId());
        response.setDate(post.getDate());
        response.setTimeStart(post.getTimeStart());
        response.setTimeEnd(post.getTimeEnd());
        response.setPostType(post.getPostType());
        response.setSkillLevel(post.getSkillLevel());
        response.setCostSharing(post.getCostSharing());
        response.setMessage(post.getMessage());
        response.setStatus(post.getStatus());
        response.setRequestCount(matchRequestRepository.countByPostId(post.getId()));
        response.setAcceptedRequestId(matchRequestRepository.findFirstByPostIdAndStatus(post.getId(), Enums.RequestStatus.ACCEPTED)
                .map(MatchRequest::getId)
                .orElse(null));
        response.setCreatedAt(post.getCreatedAt());
        return response;
    }

    private <T> Map<Long, T> mapById(List<T> items, Function<T, Long> idExtractor) {
        if (items.isEmpty()) {
            return Collections.emptyMap();
        }

        return items.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
    }

    private List<Long> nonNullList(Long id) {
        if (id == null) {
            return List.of();
        }

        return List.of(id);
    }

    private List<Long> collectIds(Collection<MatchPost> posts, Function<MatchPost, Long> extractor) {
        return posts.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    @Override
    public List<RecommendedMatchResponse> getSmartRecommendations(String playstyleNote) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy User"));
        int currentTrust = currentUser.getTrustScore() != null ? currentUser.getTrustScore() : 100;

        Pageable top15 = PageRequest.of(0, 15);
        Page<MatchPost> rawMatchesPage = matchPostRepository.findPotentialMatches(currentUserId, top15);
        if (rawMatchesPage.isEmpty()) {
            return List.of();
        }

        List<MatchPost> top15Matches = rawMatchesPage.getContent();

        List<AiOpponentDto> aiInputData = top15Matches.stream()
                .map(m -> {
                    int opponentTrust = m.getUser() != null && m.getUser().getTrustScore() != null
                            ? m.getUser().getTrustScore()
                            : 100;

                    return new AiOpponentDto(m.getId(), m.getMessage(), opponentTrust);
                })
                .toList();

        List<AiRecommendationResult> aiResults = groqAiService.recommendOpponents(
                playstyleNote, currentTrust, aiInputData
        );

        return aiResults.stream().map(aiRes -> {
            MatchPost fullMatchInfo = top15Matches.stream()
                    .filter(m -> m.getId().equals(aiRes.getMatchId()))
                    .findFirst()
                    .orElse(null);

            if (fullMatchInfo == null) return null;

            RecommendedMatchResponse response = new RecommendedMatchResponse();
            response.setMatchId(fullMatchInfo.getId());
            response.setOpponentNote(fullMatchInfo.getMessage());
            response.setAiExplanation(aiRes.getAiReason());
            return response;
        }).filter(Objects::nonNull).toList();
    }
}
