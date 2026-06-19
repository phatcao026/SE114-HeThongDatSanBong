package com.example.timsanbong.ui.owner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.repository.OwnerBookingRepository;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.Collections;
import java.util.List;

public class OwnerBookingViewModel extends AndroidViewModel {
    private final OwnerBookingRepository repository = new OwnerBookingRepository();
    private final MutableLiveData<List<Booking>> bookings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Long> busyBookingId = new MutableLiveData<>(-1L);

    public OwnerBookingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Booking>> getBookings() {
        return bookings;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Long> getBusyBookingId() {
        return busyBookingId;
    }

    public void loadBookings() {
        loading.setValue(true);
        repository.getOwnerBookings(getApplication(), new RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> data) {
                loading.setValue(false);
                bookings.setValue(data == null ? Collections.emptyList() : data);
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                message.setValue(error);
            }
        });
    }

    public void checkInBooking(long bookingId) {
        mutateBooking(bookingId, "Check-in thành công.", new RepositoryMutation() {
            @Override
            public void run(RepositoryCallback<Booking> callback) {
                repository.checkInBooking(getApplication(), bookingId, callback);
            }
        });
    }

    public void checkOutBooking(long bookingId) {
        mutateBooking(bookingId, "Thu nốt tiền thành công.", new RepositoryMutation() {
            @Override
            public void run(RepositoryCallback<Booking> callback) {
                repository.checkOutBooking(getApplication(), bookingId, callback);
            }
        });
    }

    public void markNoShow(long bookingId) {
        mutateBooking(bookingId, "Đã đánh dấu no-show.", new RepositoryMutation() {
            @Override
            public void run(RepositoryCallback<Booking> callback) {
                repository.markNoShow(getApplication(), bookingId, callback);
            }
        });
    }

    public void confirmBooking(long bookingId) {
        mutateBooking(bookingId, "Xác nhận booking thành công.", new RepositoryMutation() {
            @Override
            public void run(RepositoryCallback<Booking> callback) {
                repository.confirmBooking(getApplication(), bookingId, callback);
            }
        });
    }

    public void completeBooking(long bookingId) {
        mutateBooking(bookingId, "Hoàn tất booking thành công.", new RepositoryMutation() {
            @Override
            public void run(RepositoryCallback<Booking> callback) {
                repository.completeBooking(getApplication(), bookingId, callback);
            }
        });
    }

    public void cancelBooking(long bookingId) {
        mutateBooking(bookingId, "Hủy booking thành công.", new RepositoryMutation() {
            @Override
            public void run(RepositoryCallback<Booking> callback) {
                repository.cancelBooking(getApplication(), bookingId, callback);
            }
        });
    }

    private void mutateBooking(long bookingId, String successMessage, RepositoryMutation mutation) {
        if (bookingId <= 0) {
            message.setValue("Booking khong hop le.");
            return;
        }
        busyBookingId.setValue(bookingId);
        loading.setValue(true);
        mutation.run(new RepositoryCallback<Booking>() {
            @Override
            public void onSuccess(Booking data) {
                loading.setValue(false);
                busyBookingId.setValue(-1L);
                message.setValue(data != null && data.getStatus() != null
                        ? successMessage + " Trạng thái mới: " + data.getStatus()
                        : successMessage);
                loadBookings();
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                busyBookingId.setValue(-1L);
                message.setValue(error);
            }
        });
    }

    private interface RepositoryMutation {
        void run(RepositoryCallback<Booking> callback);
    }
}
