package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerBookingRepository {

    public void getOwnerBookings(Context context, RepositoryCallback<List<Booking>> callback) {
        ApiClient.getService(context).getOwnerBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tải được lịch đặt của chủ sân.");
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void confirmBooking(Context context, long bookingId, RepositoryCallback<Booking> callback) {
        updateBookingStatus(context, bookingId, callback, "confirm");
    }

    public void completeBooking(Context context, long bookingId, RepositoryCallback<Booking> callback) {
        updateBookingStatus(context, bookingId, callback, "complete");
    }

    public void cancelBooking(Context context, long bookingId, RepositoryCallback<Booking> callback) {
        updateBookingStatus(context, bookingId, callback, "cancel");
    }

    public void checkInBooking(Context context, long bookingId, RepositoryCallback<Booking> callback) {
        if (bookingId <= 0) {
            callback.onError("Booking khong hop le.");
            return;
        }
        ApiClient.getService(context).checkInOwnerBooking(bookingId).enqueue(bookingCallback(callback));
    }

    public void checkOutBooking(Context context, long bookingId, RepositoryCallback<Booking> callback) {
        if (bookingId <= 0) {
            callback.onError("Booking khong hop le.");
            return;
        }
        ApiClient.getService(context).checkOutOwnerBooking(bookingId).enqueue(bookingCallback(callback));
    }

    public void markNoShow(Context context, long bookingId, RepositoryCallback<Booking> callback) {
        if (bookingId <= 0) {
            callback.onError("Booking khong hop le.");
            return;
        }
        ApiClient.getService(context).markOwnerBookingNoShow(bookingId).enqueue(bookingCallback(callback));
    }

    private void updateBookingStatus(Context context, long bookingId, RepositoryCallback<Booking> callback,
                                     String action) {
        if (bookingId <= 0) {
            callback.onError("Booking khong hop le.");
            return;
        }
        Call<Booking> call;
        if ("confirm".equals(action)) {
            call = ApiClient.getService(context).confirmOwnerBooking(bookingId);
        } else if ("complete".equals(action)) {
            call = ApiClient.getService(context).completeOwnerBooking(bookingId);
        } else {
            call = ApiClient.getService(context).cancelOwnerBooking(bookingId);
        }

        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Cập nhật booking không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    private Callback<Booking> bookingCallback(RepositoryCallback<Booking> callback) {
        return new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Cập nhật booking không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        };
    }
}
