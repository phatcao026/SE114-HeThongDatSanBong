package com.example.fieldbooking.network;

import com.example.fieldbooking.model.AuthResponse;
import com.example.fieldbooking.model.Booking;
import com.example.fieldbooking.model.Field;
import com.example.fieldbooking.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    @GET("bookings/my")
    Call<List<Booking>> getMyBookings();

    @DELETE("bookings/{id}")
    Call<Void> cancelBooking(@Path("id") long id);

    // Profile
    @GET("users/me")
    Call<User> getMyProfile();
}
