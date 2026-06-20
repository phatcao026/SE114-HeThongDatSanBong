package com.example.backend.service.impl;

import com.example.backend.dto.response.AdminDashboardOverviewResponse;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.FieldResponse;
import com.example.backend.dto.response.MatchPostResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.MatchPost;
import com.example.backend.entity.MatchRequest;
import com.example.backend.entity.Payment;
import com.example.backend.entity.Review;
import com.example.backend.entity.TimeSlot;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.MatchPostRepository;
import com.example.backend.repository.MatchRequestRepository;
import com.example.backend.repository.MessageRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TimeSlotRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AdminDashboardService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PaymentRepository paymentRepository;
    private final MatchPostRepository matchPostRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final ReviewRepository reviewRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public AdminDashboardServiceImpl(UserRepository userRepository,
                                     FieldRepository fieldRepository,
                                     BookingRepository bookingRepository,
                                     TimeSlotRepository timeSlotRepository,
                                     PaymentRepository paymentRepository,
                                     MatchPostRepository matchPostRepository,
                                     MatchRequestRepository matchRequestRepository,
                                     ReviewRepository reviewRepository,
                                     ConversationRepository conversationRepository,
                                     MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.paymentRepository = paymentRepository;
        this.matchPostRepository = matchPostRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.reviewRepository = reviewRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public AdminDashboardOverviewResponse getOverview() {
        ensureAdminRole();

        AdminDashboardOverviewResponse response = new AdminDashboardOverviewResponse();
        response.setTotalUsers(userRepository.count());
        response.setPlayerCount(userRepository.countByRole(Enums.UserRole.PLAYER));
        response.setOwnerCount(userRepository.countByRole(Enums.UserRole.OWNER));
        response.setAdminCount(userRepository.countByRole(Enums.UserRole.ADMIN));

        response.setTotalFields(fieldRepository.count());
        response.setAvailableFields(fieldRepository.countByStatus(Enums.FieldStatus.AVAILABLE));
        response.setMaintenanceFields(fieldRepository.countByStatus(Enums.FieldStatus.MAINTENANCE));
        response.setBookedFields(fieldRepository.countByStatus(Enums.FieldStatus.BOOKED));

        response.setTotalBookings(bookingRepository.count());
        response.setPendingBookings(bookingRepository.countByStatus(Enums.BookingStatus.PENDING));
        response.setDepositPaidBookings(bookingRepository.countByStatus(Enums.BookingStatus.DEPOSIT_PAID));
        response.setConfirmedBookings(bookingRepository.countByStatus(Enums.BookingStatus.CONFIRMED));
        response.setCancelledBookings(bookingRepository.countByStatus(Enums.BookingStatus.CANCELLED));
        response.setCompletedBookings(bookingRepository.countByStatus(Enums.BookingStatus.COMPLETED));

        response.setTotalPayments(paymentRepository.count());
        response.setPendingPayments(paymentRepository.countByStatus(Enums.PaymentStatus.PENDING));
        response.setSuccessfulPayments(paymentRepository.countByStatus(Enums.PaymentStatus.SUCCESS));
        response.setFailedPayments(paymentRepository.countByStatus(Enums.PaymentStatus.FAILED));
        response.setRefundedPayments(paymentRepository.countByStatus(Enums.PaymentStatus.REFUNDED));
        response.setTotalRevenue(paymentRepository.calculateSuccessfulPaymentRevenue());

        response.setTotalMatchPosts(matchPostRepository.count());
        response.setOpenMatchPosts(matchPostRepository.countByStatus(Enums.PostStatus.OPEN));
        response.setMatchedMatchPosts(matchPostRepository.countByStatus(Enums.PostStatus.MATCHED));
        response.setClosedMatchPosts(matchPostRepository.countByStatus(Enums.PostStatus.CLOSED));
        response.setExpiredMatchPosts(matchPostRepository.countByStatus(Enums.PostStatus.EXPIRED));

        response.setTotalReviews(reviewRepository.count());
        response.setPendingReviews(reviewRepository.countByStatus(Enums.ReviewStatus.PENDING_ADMIN_REVIEW));
        response.setPenalizedReviews(reviewRepository.countByStatus(Enums.ReviewStatus.PENALIZED));
        response.setTotalConversations(conversationRepository.count());
        response.setTotalMessages(messageRepository.count());
        response.setGeneratedAt(LocalDateTime.now());
        return response;
    }

    @Override
    public List<UserResponse> getUsers(Enums.UserRole role, Integer minTrustScore) {
        ensureAdminRole();
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> minTrustScore == null
                        || (user.getTrustScore() != null && user.getTrustScore() >= minTrustScore))
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    public List<FieldResponse> getFields(Enums.FieldStatus status) {
        ensureAdminRole();
        List<Field> fields = status == null
                ? fieldRepository.findAllByOrderByCreatedAtDesc()
                : fieldRepository.findByStatusOrderByCreatedAtDesc(status);
        return fields.stream().map(this::toFieldResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookings(Enums.BookingStatus status) {
        ensureAdminRole();
        List<Booking> bookings = status == null
                ? bookingRepository.findAllByOrderByCreatedAtDesc()
                : bookingRepository.findByStatusOrderByCreatedAtDesc(status);
        return toBookingResponses(bookings);
    }

    @Override
    public List<PaymentResponse> getPayments(Enums.PaymentStatus status) {
        ensureAdminRole();
        List<Payment> payments = status == null
                ? paymentRepository.findAllByOrderByCreatedAtDesc()
                : paymentRepository.findByStatusOrderByCreatedAtDesc(status);
        return payments.stream().map(this::toPaymentResponse).toList();
    }

    @Override
    public List<MatchPostResponse> getMatchPosts(Enums.PostStatus status) {
        ensureAdminRole();
        List<MatchPost> posts = status == null
                ? matchPostRepository.findAllByOrderByCreatedAtDesc()
                : matchPostRepository.findByStatusOrderByCreatedAtDesc(status);
        return toMatchPostResponses(posts);
    }

    @Override
    public List<ReviewResponse> getReviews(Enums.ReviewStatus status) {
        ensureAdminRole();
        List<Review> reviews = status == null
                ? reviewRepository.findAllByOrderByCreatedAtDesc()
                : reviewRepository.findByStatusOrderByCreatedAtDesc(status);
        return toReviewResponses(reviews);
    }

    private void ensureAdminRole() {
        if (!TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Only admins can access dashboard data");
        }
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setTrustScore(user.getTrustScore());
        response.setIsLocked(user.getIsLocked());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private FieldResponse toFieldResponse(Field field) {
        FieldResponse response = new FieldResponse();
        response.setId(field.getId());
        response.setName(field.getName());
        response.setDescription(field.getDescription());
        response.setType(field.getType());
        response.setStatus(field.getStatus());
        response.setCoverImage(field.getCoverImage());
        response.setCreatedAt(field.getCreatedAt());
        response.setUpdatedAt(field.getUpdatedAt());
        return response;
    }

    private List<BookingResponse> toBookingResponses(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return List.of();
        }

        Map<Long, Field> fields = mapById(fieldRepository.findAllById(collectIds(bookings, Booking::getFieldId)), Field::getId);
        Map<Long, TimeSlot> timeSlots = mapById(timeSlotRepository.findAllById(collectIds(bookings, Booking::getTimeSlotId)), TimeSlot::getId);

        return bookings.stream()
                .map(booking -> toBookingResponse(booking, fields.get(booking.getFieldId()), timeSlots.get(booking.getTimeSlotId())))
                .toList();
    }

    private BookingResponse toBookingResponse(Booking booking, Field field, TimeSlot timeSlot) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setFieldId(booking.getFieldId());
        response.setFieldName(field != null ? field.getName() : null);
        response.setTimeSlotId(booking.getTimeSlotId());
        response.setUserId(booking.getUserId());
        response.setStatus(booking.getStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setDepositAmount(booking.getDepositAmount());
        response.setBookingDate(booking.getBookingDate());
        response.setStartTime(timeSlot != null ? timeSlot.getStartTime() : null);
        response.setEndTime(timeSlot != null ? timeSlot.getEndTime() : null);
        response.setNote(booking.getNote());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        return response;
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBookingId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    private List<MatchPostResponse> toMatchPostResponses(List<MatchPost> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        Map<Long, User> users = mapById(userRepository.findAllById(collectIds(posts, MatchPost::getUserId)), User::getId);
        Map<Long, Field> fields = mapById(fieldRepository.findAllById(collectIds(posts, MatchPost::getFieldId)), Field::getId);

        return posts.stream()
                .map(post -> toMatchPostResponse(post, users, fields))
                .toList();
    }

    private MatchPostResponse toMatchPostResponse(MatchPost post,
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
        return response;
    }

    private List<ReviewResponse> toReviewResponses(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }

        Map<Long, User> users = mapById(userRepository.findAllById(collectReviewUserIds(reviews)), User::getId);
        Map<Long, MatchRequest> matchRequests = mapById(
                matchRequestRepository.findAllById(collectIds(reviews, Review::getMatchRequestId)),
                MatchRequest::getId
        );

        return reviews.stream()
                .map(review -> toReviewResponse(review, users, matchRequests))
                .toList();
    }

    private ReviewResponse toReviewResponse(Review review,
                                            Map<Long, User> users,
                                            Map<Long, MatchRequest> matchRequests) {
        User reviewer = users.get(review.getReviewerId());
        User reviewee = users.get(review.getRevieweeId());
        MatchRequest matchRequest = matchRequests.get(review.getMatchRequestId());

        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setReviewerId(review.getReviewerId());
        response.setReviewerName(reviewer != null ? reviewer.getFullName() : null);
        response.setRevieweeId(review.getRevieweeId());
        response.setRevieweeName(reviewee != null ? reviewee.getFullName() : null);
        response.setRevieweeTrustScore(reviewee != null ? reviewee.getTrustScore() : null);
        response.setMatchRequestId(review.getMatchRequestId());
        response.setMatchPostId(matchRequest != null ? matchRequest.getPostId() : null);
        response.setScoreChange(review.getScoreChange());
        response.setReason(review.getReason());
        response.setAiSuggestedPenalty(review.getAiSuggestedPenalty());
        response.setStatus(review.getStatus());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    private <T> Map<Long, T> mapById(List<T> items, Function<T, Long> idExtractor) {
        if (items.isEmpty()) {
            return Collections.emptyMap();
        }

        return items.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
    }

    private <T> List<Long> collectIds(Collection<T> items, Function<T, Long> idExtractor) {
        return items.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> collectReviewUserIds(List<Review> reviews) {
        return reviews.stream()
                .flatMap(review -> Stream.of(review.getReviewerId(), review.getRevieweeId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng"));
        if (user.getRole() == Enums.UserRole.ADMIN) {
            throw new AppException(400, "Không thể khóa tài khoản quản trị viên");
        }
        user.setIsLocked(true);
        userRepository.save(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng"));
        user.setIsLocked(false);
        userRepository.save(user);
    }
}
