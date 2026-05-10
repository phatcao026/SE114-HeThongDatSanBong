package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {

    public void getMyBookings(Context context, RepositoryCallback<List<Booking>> callback) {
        ApiClient.getService(context).getMyBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tải được danh sách đặt sân.");
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void createBooking(Context context, Map<String, Object> body, RepositoryCallback<Booking> callback) {
        ApiClient.getService(context).createBooking(body).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Đặt sân không thành công. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    public void cancelBooking(Context context, long bookingId, RepositoryCallback<Void> callback) {
        ApiClient.getService(context).cancelBooking(bookingId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Hủy đặt sân không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }
}
