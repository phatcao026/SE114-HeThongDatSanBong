package com.example.timsanbong.data.api;

import com.example.timsanbong.data.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("auth/login")
    Call<AuthResponse> login(@Body Map<String, String> credentials);

    @POST("auth/register")
    Call<AuthResponse> register(@Body Map<String, String> body);

    @POST("auth/send-register-otp")
    Call<Void> sendRegisterOtp(@Body Map<String, String> body);

    @POST("auth/forgot-password")
    Call<Void> forgotPassword(@Body Map<String, String> body);

    @POST("auth/verify-otp")
    Call<Void> verifyOtp(@Body Map<String, String> body);

    @POST("auth/reset-password")
    Call<Void> resetPassword(@Body Map<String, String> body);

    // Users / profile
    @GET("users/me")
    Call<User> getMyProfile();

    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") long id, @Body User user);

    // Fields
    @GET("fields")
    Call<List<Field>> getFields();

    @GET("fields/{id}")
    Call<Field> getFieldById(@Path("id") long id);

    @GET("fields/{id}/availability")
    Call<List<TimeSlotResponse>> getTimeslots(@Path("id") long id, @Query("date") String date);

    // Player bookings
    @POST("bookings")
    Call<Booking> createBooking(@Body BookingRequest body);

    @GET("bookings")
    Call<List<Booking>> getBookings();

    @GET("bookings/{id}")
    Call<Booking> getBookingById(@Path("id") long id);

    @PUT("bookings/{id}/cancel")
    Call<Booking> cancelMyBooking(@Path("id") long id);

    // Payments
    @GET("payments")
    Call<List<PaymentResponse>> getMyPayments();

    @GET("payments/booking/{bookingId}")
    Call<List<PaymentResponse>> getBookingPayments(@Path("bookingId") long bookingId);

    @POST("payments/create-session/{bookingId}")
    Call<PaymentResponse> createCheckoutSession(@Path("bookingId") long bookingId);

    @POST("payments/verify-session")
    Call<PaymentResponse> verifyCheckoutSession(@Query("sessionId") String sessionId);

    // Match posts
    @GET("match-posts")
    Call<List<MatchPost>> getMatchPosts(@Query("postType") String postType);

    @POST("match-posts")
    Call<MatchPost> createMatchPost(@Body MatchPostRequest request);

    @PUT("match-posts/{id}")
    Call<MatchPost> updateMatchPost(@Path("id") long id, @Body MatchPostRequest request);

    @DELETE("match-posts/{id}")
    Call<Void> deleteMatchPost(@Path("id") long id);

    @GET("match-posts/my")
    Call<List<MatchPost>> getMyMatchPosts();

    @GET("match-posts/recommendations")
    Call<List<RecommendedMatch>> getSmartRecommendations(
            @Query("playstyleNote") String playstyleNote,
            @Query("teamName") String teamName,
            @Query("date") String date,
            @Query("timeStart") String timeStart,
            @Query("timeEnd") String timeEnd,
            @Query("skillLevel") String skillLevel,
            @Query("hasField") Boolean hasField,
            @Query("postType") String postType,
            @Query("position") String position
    );

    // Match requests
    @POST("match-posts/{id}/requests")
    Call<MatchRequestResponse> createMatchRequest(@Path("id") long postId, @Body Map<String, String> body);

    @PUT("match-requests/{id}/status")
    Call<MatchRequestResponse> updateMatchRequestStatus(@Path("id") long id,
                                                        @Body MatchRequestStatusUpdate update);

    // Teams
    @GET("teams/my")
    Call<List<TeamResponse>> getMyTeams();

    @POST("teams")
    Call<TeamResponse> createTeam(@Body TeamRequest request);

    @PUT("teams/{id}")
    Call<TeamResponse> updateTeam(@Path("id") long id, @Body TeamRequest request);

    @DELETE("teams/{id}")
    Call<Void> deleteTeam(@Path("id") long id);

    @GET("teams/invitations/my")
    Call<List<InvitationResponse>> getInvitations();

    @PUT("teams/invitations/{id}")
    Call<Void> respondToInvitation(@Path("id") long id, @Body Map<String, String> body);

    // Conversations & messages
    @GET("conversations")
    Call<List<Conversation>> getConversations();

    @POST("conversations/direct")
    Call<Conversation> createDirectConversation(@Body Map<String, Long> body);

    @GET("conversations/{id}/messages")
    Call<List<ChatMessage>> getMessages(@Path("id") long conversationId);

    @POST("conversations/{id}/messages")
    Call<ChatMessage> sendMessage(@Path("id") long conversationId, @Body MessageRequest request);

    // Notifications
    @GET("notifications")
    Call<List<AppNotification>> getNotifications(@Query("isRead") Boolean isRead);

    @GET("notifications/unread-count")
    Call<UnreadCountResponse> getUnreadNotificationCount();

    @PUT("notifications/{id}/read")
    Call<Void> markNotificationRead(@Path("id") long id);

    @PUT("notifications/read-all")
    Call<Void> markAllNotificationsRead();

    @POST("notifications/fcm-token")
    Call<Void> registerFcmToken(@Query("fcmToken") String fcmToken);

    @DELETE("notifications/fcm-token")
    Call<Void> deregisterFcmToken(@Query("fcmToken") String fcmToken);

    // Reviews
    @POST("fairplay/reviews")
    Call<Void> submitReview(@Body ReviewRequest request);

    @GET("fairplay/my-submitted")
    Call<List<Long>> getMySubmittedReviews();

    @GET("fairplay/my-reviews")
    Call<List<OpponentReviewResponse>> getMyFairplayReviews();

    @POST("reviews/field")
    Call<FieldReviewResponse> createFieldReview(@Body FieldReviewRequest request);

    @GET("reviews/field/{fieldId}")
    Call<List<FieldReviewResponse>> getFieldReviews(@Path("fieldId") long fieldId);

    // Owner fields
    @GET("fields/mine")
    Call<List<Field>> getOwnerFields(@Query("date") String date);

    @GET("fields")
    Call<List<Field>> getFields(
            @Query("type") String type,
            @Query("minPrice") BigDecimal minPrice,
            @Query("maxPrice") BigDecimal maxPrice);

    // GET /api/fields/page
//    @GET("fields/page")
//    Call<Page<Field>> getFieldsPage(
//            @Query("type") String type,
//            @Query("name") String name,
//            @Query("page") int page,
//            @Query("size") int size
//    );

    // GET /api/fields/{id}
    @GET("fields/{id}")
    Call<Field> getFieldById(@Path("id") String id);

    // GET /api/fields/{id}/availability
    @GET("fields/{id}/availability")
    Call<List<TimeSlotResponse>> getFieldAvailability(
            @Path("id") String id,
            @Query("date") String date // Format: "yyyy-MM-dd"
    );

    @POST("fields")
    Call<Field> createOwnerField(@Body FieldCreateRequest request);

    @PUT("fields/{id}")
    Call<Field> updateOwnerField(@Path("id") long id, @Body FieldUpdateRequest request);

    @DELETE("fields/{id}")
    Call<Field> deleteOwnerField(@Path("id") long id);

    @POST("fields/{id}/time-slots")
    Call<TimeSlot> createOwnerTimeSlot(@Path("id") long fieldId, @Body TimeSlotCreateRequest request);

    @PUT("fields/{id}/time-slots/{slotId}")
    Call<TimeSlot> updateOwnerTimeSlot(@Path("id") long fieldId,
                                       @Path("slotId") long slotId,
                                       @Body TimeSlotUpdateRequest request);

    @DELETE("fields/{id}/time-slots/{slotId}")
    Call<TimeSlot> deleteOwnerTimeSlot(@Path("id") long fieldId, @Path("slotId") long slotId);

    // Owner bookings
    @GET("bookings/owner")
    Call<List<Booking>> getOwnerBookings();

    @PUT("bookings/{id}/confirm")
    Call<Booking> confirmOwnerBooking(@Path("id") long id);

    @PUT("bookings/{id}/complete")
    Call<Booking> completeOwnerBooking(@Path("id") long id);

    @PUT("bookings/{id}/cancel")
    Call<Booking> cancelOwnerBooking(@Path("id") long id);

    @PUT("bookings/{id}/check-in")
    Call<Booking> checkInOwnerBooking(@Path("id") long id);

    @POST("bookings/{id}/check-out")
    Call<Booking> checkOutOwnerBooking(@Path("id") long id);

    @PUT("bookings/{id}/no-show")
    Call<Booking> markOwnerBookingNoShow(@Path("id") long id);

    // Admin
    @GET("admin/dashboard/overview")
    Call<AdminDashboardOverviewResponse> getAdminOverview();

    @GET("admin/users")
    Call<List<User>> getAdminUsers();

    @GET("admin/payments")
    Call<List<PaymentResponse>> getAdminPayments();

    @GET("admin/bookings")
    Call<List<Booking>> getAdminBookings();

    @GET("admin/fields")
    Call<List<Field>> getAdminFields();

    @GET("admin/match-posts")
    Call<List<MatchPost>> getAdminMatchPosts();

    @GET("admin/reviews")
    Call<List<ReviewResponse>> getAdminReviews();

    @GET("admin/field-reviews")
    Call<List<FieldReviewResponse>> getAdminFieldReviews();

    @PUT("admin/users/{id}/lock")
    Call<Void> lockUser(@Path("id") long id);

    @PUT("admin/users/{id}/unlock")
    Call<Void> unlockUser(@Path("id") long id);

    @GET("admin/fairplay/pending")
    Call<List<OpponentReviewResponse>> getAdminFairplayPending();

    @GET("admin/fairplay/processed")
    Call<List<OpponentReviewResponse>> getAdminFairplayProcessed();

    @PUT("admin/fairplay/resolve/{id}")
    Call<Void> resolveFairplayReview(@Path("id") long id, @Body Map<String, Object> body);
}
