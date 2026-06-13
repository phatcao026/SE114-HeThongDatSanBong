package com.example.timsanbong.data.api;

import com.example.timsanbong.data.model.AdminDashboardOverviewResponse;
import com.example.timsanbong.data.model.AuthResponse;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldCreateRequest;
import com.example.timsanbong.data.model.FieldUpdateRequest;
import com.example.timsanbong.data.model.PaymentResponse;
import com.example.timsanbong.data.model.TimeSlot;
import com.example.timsanbong.data.model.TimeSlotCreateRequest;
import com.example.timsanbong.data.model.TimeSlotUpdateRequest;
import com.example.timsanbong.data.model.User;

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

    // Fields
    @GET("fields")
    Call<List<Field>> getFields();

    @GET("fields")
    Call<List<Field>> searchFields(@Query("keyword") String keyword);

    @GET("fields/{id}")
    Call<Field> getFieldById(@Path("id") long id);

    // Bookings
    @POST("bookings")
    Call<Booking> createBooking(@Body Map<String, Object> body);

    @GET("bookings")
    Call<List<Booking>> getMyBookings();

    @PUT("bookings/{id}/cancel")
    Call<Booking> cancelBooking(@Path("id") long id);

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
    Call<TimeSlot> updateOwnerTimeSlot(@Path("id") long fieldId, @Path("slotId") long slotId,
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

    // Profile
    @GET("users/me")
    Call<User> getMyProfile();

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
}
