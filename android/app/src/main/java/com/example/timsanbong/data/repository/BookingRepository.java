package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {

    public void getMyBookings(Context context, RepositoryCallback<List<Booking>> callback) {
        if (com.example.timsanbong.utils.Constants.MOCK_MODE) {
            callback.onSuccess(getMockBookings());
            return;
        }
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

    public void createBooking(Context context, long fieldId, String bookingDate, String startTime, String endTime,
                              RepositoryCallback<Booking> callback) {
        if (com.example.timsanbong.utils.Constants.MOCK_MODE) {
            Field field = getMockFieldById(fieldId);
            double totalPrice = calculateTotalPrice(field, startTime, endTime);
            Booking booking = new Booking(System.currentTimeMillis(), field, null, bookingDate, startTime, endTime,
                    totalPrice, "PENDING");
            callback.onSuccess(booking);
            return;
        }
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("fieldId", fieldId);
        body.put("bookingDate", bookingDate);
        body.put("startTime", startTime);
        body.put("endTime", endTime);

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
        if (com.example.timsanbong.utils.Constants.MOCK_MODE) {
            callback.onSuccess(null);
            return;
        }
        ApiClient.getService(context).cancelBooking(bookingId).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Hủy đặt sân không thành công.");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ.");
            }
        });
    }

    private List<Booking> getMockBookings() {
        List<Booking> bookings = new java.util.ArrayList<>();
        Field fieldA = getMockFieldById(1);
        Field fieldB = getMockFieldById(2);
        bookings.add(new Booking(101, fieldA, null, "2026-05-12", "18:00", "19:30",
                calculateTotalPrice(fieldA, "18:00", "19:30"), "CONFIRMED"));
        bookings.add(new Booking(102, fieldB, null, "2026-05-13", "20:00", "21:00",
                calculateTotalPrice(fieldB, "20:00", "21:00"), "PENDING"));
        return bookings;
    }

    private Field getMockFieldById(long id) {
        for (Field field : new FieldRepository().getMockFields()) {
            if (field.getId() == id) {
                return field;
            }
        }
        return new Field(id, "Sân Bóng", "---", 200000, "https://via.placeholder.com/300",
                "Sân cỏ nhân tạo", "5 người", true);
    }

    private double calculateTotalPrice(Field field, String startTime, String endTime) {
        if (field == null) {
            return 0;
        }
        int startMinutes = parseMinutes(startTime);
        int endMinutes = parseMinutes(endTime);
        if (startMinutes < 0 || endMinutes <= startMinutes) {
            return 0;
        }
        double hours = (endMinutes - startMinutes) / 60.0;
        return field.getPricePerHour() * hours;
    }

    private int parseMinutes(String time) {
        if (time == null || time.trim().isEmpty()) {
            return -1;
        }
        String[] parts = time.split(":");
        if (parts.length != 2) {
            return -1;
        }
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }
}
