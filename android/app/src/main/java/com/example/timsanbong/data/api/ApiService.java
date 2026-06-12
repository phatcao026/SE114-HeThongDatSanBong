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

    // Users
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

    // Bookings
    @POST("bookings")
    Call<Booking> createBooking(@Body BookingRequest body);

    @GET("bookings")
    Call<List<Booking>> getBookings();

    @GET("bookings/{id}")
    Call<Booking> getBookingById(@Path("id") long id);

    @PUT("bookings/{id}/cancel")
    Call<Void> cancelBooking(@Path("id") long id);

    // Payments
    @POST("payments/create-session/{bookingId}")
    Call<CheckoutSessionResponse> createCheckoutSession(@Path("bookingId") long bookingId);

    // Match Posts
    @GET("match-posts")
    Call<PageResponse<MatchPost>> getMatchPosts(@Query("page") int page, @Query("size") int size, @Query("postType") String postType);

    @POST("match-posts")
    Call<MatchPost> createMatchPost(@Body MatchPostRequest request);

    @PUT("match-posts/{id}")
    Call<MatchPost> updateMatchPost(@Path("id") long id, @Body MatchPostRequest request);

    @DELETE("match-posts/{id}")
    Call<Void> deleteMatchPost(@Path("id") long id);

    @GET("match-posts/me")
    Call<PageResponse<MatchPost>> getMyMatchPosts(@Query("page") int page, @Query("size") int size);

    // Match Requests
    @POST("match-requests")
    Call<MatchRequestResponse> createMatchRequest(@Body Map<String, Long> body); // matchPostId

    @PUT("match-requests/{id}/status")
    Call<MatchRequestResponse> updateMatchRequestStatus(@Path("id") long id, @Body MatchRequestStatusUpdate update);

    // Teams
    @GET("teams/me")
    Call<List<TeamResponse>> getMyTeams();

    @POST("teams")
    Call<TeamResponse> createTeam(@Body TeamRequest request);

    @PUT("teams/{id}")
    Call<TeamResponse> updateTeam(@Path("id") long id, @Body TeamRequest request);

    @DELETE("teams/{id}")
    Call<Void> deleteTeam(@Path("id") long id);

    @GET("teams/invitations/me")
    Call<List<InvitationResponse>> getInvitations();

    @PUT("teams/invitations/{id}")
    Call<Void> respondToInvitation(@Path("id") long id, @Body Map<String, String> body); // status

    // Conversations & Messages
    @GET("conversations")
    Call<List<Conversation>> getConversations();

    @GET("conversations/unread-count")
    Call<UnreadCountResponse> getUnreadConversationCount();

    @GET("messages")
    Call<PageResponse<ChatMessage>> getMessages(@Query("conversationId") long conversationId, @Query("page") int page, @Query("size") int size);

    @POST("messages")
    Call<ChatMessage> sendMessage(@Body MessageRequest request);

    // Notifications
    @GET("notifications")
    Call<PageResponse<AppNotification>> getNotifications(@Query("page") int page, @Query("size") int size);

    @GET("notifications/unread-count")
    Call<UnreadCountResponse> getUnreadNotificationCount();

    @PUT("notifications/{id}/read")
    Call<Void> markNotificationRead(@Path("id") long id);

    @PUT("notifications/read-all")
    Call<Void> markAllNotificationsRead();

    // Reviews
    @POST("fairplay/reviews")
    Call<Void> submitReview(@Body ReviewRequest request);
}
