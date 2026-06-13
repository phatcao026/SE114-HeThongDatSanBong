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

    public void confirmBooking(long bookingId) {
        loading.setValue(true);
        repository.confirmBooking(getApplication(), bookingId, bookingMutationCallback());
    }

    public void completeBooking(long bookingId) {
        loading.setValue(true);
        repository.completeBooking(getApplication(), bookingId, bookingMutationCallback());
    }

    public void cancelBooking(long bookingId) {
        loading.setValue(true);
        repository.cancelBooking(getApplication(), bookingId, bookingMutationCallback());
    }

    private RepositoryCallback<Booking> bookingMutationCallback() {
        return new RepositoryCallback<Booking>() {
            @Override
            public void onSuccess(Booking data) {
                loading.setValue(false);
                message.setValue("Cap nhat booking thanh cong.");
                loadBookings();
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                message.setValue(error);
            }
        };
    }
}
