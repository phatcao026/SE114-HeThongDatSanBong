package com.example.backend.service.impl;

import com.example.backend.dto.request.MatchPostCreateRequest;
import com.example.backend.dto.request.MatchPostUpdateRequest;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.TimeSlot;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
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
    private final UserRepository userRepository;
    private final GroqAiService groqAiService;

    public MatchPostServiceImpl(MatchPostRepository matchPostRepository,
                                MatchRequestRepository matchRequestRepository,
                                BookingRepository bookingRepository,
                                FieldRepository fieldRepository,
                                TimeSlotRepository timeSlotRepository,
                                UserRepository userRepository,
                                GroqAiService groqAiService) {
        this.matchPostRepository = matchPostRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.groqAiService = groqAiService;
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<MatchPostResponse> getMyMatchPosts() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "User not found"));

        return toResponses(matchPostRepository.findByUserIdOrderByCreatedAtDesc(currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
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

        boolean hasField = request.getHasField() == null || request.getHasField();
        post.setHasField(hasField);

        Long bookingId = request.getBookingId();
        Long fieldId = request.getFieldId();

        if (hasField) {
            if (bookingId == null && fieldId == null) {
                throw new AppException(400, "Field or Booking is required when hasField is true");
            }
        } else {
            bookingId = null;
            fieldId = null;
        }

        if (request.getPostType() == Enums.PostType.FIND_OPPONENT) {
            post.setNeededMembers(1);
            post.setTargetPositions(null);
        } else {
            Integer needed = request.getNeededMembers();
            post.setNeededMembers((needed == null || needed <= 0) ? 1 : needed);
            post.setTargetPositions(cleanOptional(request.getTargetPositions()));
        }
        post.setJoinedMembers(0);
        post.setAgeRange(cleanOptional(request.getAgeRange()));
        post.setTeamName(cleanOptional(request.getTeamName()));

        // Essential match info for BOTH now (date, timeStart)
        post.setDate(request.getDate());
        post.setTimeStart(request.getTimeStart());
        post.setTimeEnd(request.getTimeEnd());

        applyBookingOrManualSchedule(
                post,
                bookingId,
                fieldId,
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

        if (request.getHasField() != null) {
            post.setHasField(request.getHasField());
        }

        if (post.getHasField()) {
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
            if (post.getBookingId() == null && post.getFieldId() == null) {
                throw new AppException(400, "Field or Booking is required when hasField is true");
            }
        } else {
            post.setBookingId(null);
            post.setFieldId(null);
            if (request.getDate() != null) post.setDate(request.getDate());
            if (request.getTimeStart() != null) post.setTimeStart(request.getTimeStart());
            if (request.getTimeEnd() != null) post.setTimeEnd(request.getTimeEnd());
        }

        if (request.getPostType() != null) {
            post.setPostType(request.getPostType());
        }

        Enums.PostType activeType = post.getPostType();
        if (activeType == Enums.PostType.FIND_OPPONENT) {
            post.setNeededMembers(1);
            post.setTargetPositions(null);
        } else {
            if (request.getNeededMembers() != null) {
                post.setNeededMembers(request.getNeededMembers() <= 0 ? 1 : request.getNeededMembers());
            }
            if (request.getTargetPositions() != null) {
                post.setTargetPositions(cleanOptional(request.getTargetPositions()));
            }
        }

        if (request.getAgeRange() != null) {
            post.setAgeRange(cleanOptional(request.getAgeRange()));
        }

        if (request.getTeamName() != null) {
            post.setTeamName(cleanOptional(request.getTeamName()));
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
        return request.getFieldId() != null
                || request.getBookingId() != null
                || request.getDate() != null
                || request.getTimeStart() != null
                || request.getTimeEnd() != null
                || request.getPostType() != null
                || request.getSkillLevel() != null
                || request.getCostSharing() != null
                || request.getMessage() != null
                || request.getNeededMembers() != null
                || request.getHasField() != null
                || request.getTargetPositions() != null
                || request.getAgeRange() != null;
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

        throw new AppException(403, "Only the post owner can manage this match post");
    }

    private MatchPostResponse toResponse(MatchPost post) {
        Map<Long, User> users = mapById(userRepository.findAllById(nonNullList(post.getUserId())), User::getId);
        Map<Long, Field> fields = mapById(fieldRepository.findAllById(nonNullList(post.getFieldId())), Field::getId);
        return toResponse(post, users, fields);
    }

    private List<MatchPostResponse> toResponses(List<MatchPost> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        Map<Long, User> users = mapById(userRepository.findAllById(collectIds(posts, MatchPost::getUserId)), User::getId);
        Map<Long, Field> fields = mapById(fieldRepository.findAllById(collectIds(posts, MatchPost::getFieldId)), Field::getId);

        return posts.stream()
                .map(post -> toResponse(post, users, fields))
                .toList();
    }

    private MatchPostResponse toResponse(MatchPost post,
                                         Map<Long, User> users,
                                         Map<Long, Field> fields) {
        MatchPostResponse response = new MatchPostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setUserName(users.containsKey(post.getUserId()) ? users.get(post.getUserId()).getFullName() : null);
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
        response.setNeededMembers(post.getNeededMembers());
        response.setJoinedMembers(post.getJoinedMembers());
        response.setConversationId(post.getConversationId());
        response.setHasField(post.getHasField());
        response.setTargetPositions(post.getTargetPositions());
        response.setAgeRange(post.getAgeRange());
        
        // Final logic for team name
        if (post.getTeamName() != null && !post.getTeamName().trim().isEmpty()) {
            response.setTeamName(post.getTeamName());
        } else if (users.containsKey(post.getUserId())) {
            response.setTeamName("Đội của " + users.get(post.getUserId()).getFullName());
        } else {
            response.setTeamName("Đội bóng");
        }
        
        if (post.getUser() != null) {
            User u = post.getUser();
            response.setTrustScore(u.getTrustScore() != null ? u.getTrustScore() : 100);
            
            // Re-calculate stats for response
            int matches = u.getBookings() != null ? (int) u.getBookings().stream()
                    .filter(b -> b.getStatus() == com.example.backend.utils.Enums.BookingStatus.COMPLETED)
                    .count() : 0;
            int noShows = u.getBookings() != null ? (int) u.getBookings().stream()
                    .filter(b -> b.getStatus() == com.example.backend.utils.Enums.BookingStatus.CANCELLED)
                    .count() : 0;
            
            response.setMatchesPlayed(matches);
            response.setNoShows(noShows);
            double calculatedRating = response.getTrustScore() / 20.0;
            response.setAverageRating(Math.round(calculatedRating * 10.0) / 10.0);
        } else {
            response.setTrustScore(100);
            response.setMatchesPlayed(0);
            response.setNoShows(0);
            response.setAverageRating(5.0);
        }

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
    public List<RecommendedMatchResponse> getSmartRecommendations(
            String playstyleNote,
            LocalDate date,
            LocalTime timeStart,
            LocalTime timeEnd,
            Enums.TeamLevel skillLevel,
            Boolean hasField,
            Enums.PostType postType,
            String position) {

        Long currentUserId = TokenUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy User"));
        int currentTrust = currentUser.getTrustScore() != null ? currentUser.getTrustScore() : 100;

        Enums.PostType activePostType = postType != null ? postType : Enums.PostType.FIND_OPPONENT;

            // Use a large page size to fetch more potential matches for AI to filter
            Pageable top50 = PageRequest.of(0, 50);
            Page<MatchPost> rawMatchesPage = matchPostRepository.findPotentialMatches(
                    currentUserId,
                    activePostType,
                    date,
                    skillLevel,
                    hasField,
                    top50
            );

            // LOG the parameters and result size for debugging
            System.out.println("DEBUG: findPotentialMatches - userId: " + currentUserId + 
                    ", type: " + activePostType + ", date: " + date + ", level: " + skillLevel + 
                    ", hasField: " + hasField + " -> Found: " + rawMatchesPage.getTotalElements());

            if (rawMatchesPage.isEmpty()) {
            // If strict filtering found nothing, try a broader search (date only) if date was provided
            if (date != null && (skillLevel != null || hasField != null)) {
                rawMatchesPage = matchPostRepository.findPotentialMatches(
                        currentUserId, activePostType, date, null, null, top50);
            }
            
            if (rawMatchesPage.isEmpty()) return List.of();
        }

        List<MatchPost> potentialMatches = rawMatchesPage.getContent();

        List<AiOpponentDto> aiInputData = potentialMatches.stream()
                .map(m -> {
                    int opponentTrust = m.getUser() != null && m.getUser().getTrustScore() != null
                            ? m.getUser().getTrustScore()
                            : 100;

                    return new AiOpponentDto(
                            m.getId(),
                            m.getMessage(),
                            opponentTrust,
                            m.getDate() != null ? m.getDate().toString() : null,
                            m.getTimeStart() != null ? m.getTimeStart().toString() : null,
                            m.getTimeEnd() != null ? m.getTimeEnd().toString() : null,
                            m.getSkillLevel() != null ? m.getSkillLevel().name() : null,
                            m.getCostSharing(),
                            m.getAgeRange(),
                            m.getHasField(),
                            m.getTargetPositions()
                    );
                })
                .toList();

        List<AiRecommendationResult> aiResults = groqAiService.recommendMatches(
                playstyleNote,
                currentTrust,
                date != null ? date.toString() : null,
                timeStart != null ? timeStart.toString() : null,
                timeEnd != null ? timeEnd.toString() : null,
                skillLevel != null ? skillLevel.name() : null,
                hasField,
                activePostType,
                position,
                aiInputData
        );

        // If AI returned no matches but database has potential matches, return database matches directly
        if (aiResults.isEmpty() && !potentialMatches.isEmpty()) {
            return potentialMatches.stream()
                    .limit(5)
                    .map(m -> {
                        RecommendedMatchResponse res = new RecommendedMatchResponse();
                        res.setMatchId(m.getId());
                        res.setOpponentNote(m.getMessage());
                        res.setAiExplanation("Dựa trên kết quả tìm kiếm từ database.");
                        res.setMatchPost(toResponse(m));
                        return res;
                    }).toList();
        }

        return aiResults.stream().map(aiRes -> {
            MatchPost fullMatchInfo = potentialMatches.stream()
                    .filter(m -> m.getId().equals(aiRes.getMatchId()))
                    .findFirst()
                    .orElse(null);

            if (fullMatchInfo == null) return null;

            RecommendedMatchResponse response = new RecommendedMatchResponse();
            response.setMatchId(fullMatchInfo.getId());
            response.setOpponentNote(fullMatchInfo.getMessage());
            response.setAiExplanation(aiRes.getAiReason());
            response.setMatchPost(toResponse(fullMatchInfo));
            return response;
        }).filter(Objects::nonNull).toList();
    }
}
