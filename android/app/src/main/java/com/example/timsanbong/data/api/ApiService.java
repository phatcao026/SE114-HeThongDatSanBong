package com.example.timsanbong.data.api;

import com.example.timsanbong.data.model.*;

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

    @POST("auth/forgot-password")
    Call<Void> forgotPassword(@Body Map<String, String> body);

    @POST("auth/verify-otp")
    Call<Void> verifyOtp(@Body Map<String, String> body);

    @POST("auth/reset-password")
    Call<Void> resetPassword(@Body Map<String, String> body);

    @GET("auth/google-url")
    Call<GoogleUrlResponse> getGoogleUrl();

    @POST("auth/google-sync")
    Call<AuthResponse> googleSync(@Body Map<String, String> body);

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
    Call<Booking> cancelBooking(@Path("id") long id);

    // Payments
    @POST("payments/create-session/{bookingId}")
    Call<PaymentResponse> createCheckoutSession(@Path("bookingId") long bookingId);

    // Match posts
    @GET("match-posts")
    Call<List<MatchPost>> getMatchPosts(@Query("postType") String postType);

    @POST("match-posts")
    Call<MatchPost> createMatchPost(@Body MatchPostRequest request);

    @PUT("match-posts/{id}")
    Call<MatchPost> updateMatchPost(@Path("id") long id, @Body MatchPostRequest request);

    @DELETE("match-posts/{id}")
    Call<Void> deleteMatchPost(@Path("id") long id);

    @GET("match-posts/me")
    Call<List<MatchPost>> getMyMatchPosts();

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

    // Reviews
    @POST("fairplay/reviews")
    Call<Void> submitReview(@Body ReviewRequest request);

    // Owner fields
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

    // Admin
    @GET("admin/overview")
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

    @GET("admin/fairplay/pending")
    Call<List<OpponentReviewResponse>> getAdminFairplayPending();

    @PUT("admin/fairplay/resolve/{id}")
    Call<Void> resolveFairplayReview(@Path("id") long id, @Body Map<String, Object> body);
}
