package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.repository.BookingRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.List;

public class BookingViewModel extends AndroidViewModel {

    private final BookingRepository bookingRepository = new BookingRepository();

    private final MutableLiveData<Resource<List<Booking>>> _bookingsState = new MutableLiveData<>();
    public LiveData<Resource<List<Booking>>> bookingsState = _bookingsState;

    private final MutableLiveData<Resource<Booking>> _bookingCreateState = new MutableLiveData<>();
    public LiveData<Resource<Booking>> bookingCreateState = _bookingCreateState;

    private final MutableLiveData<Resource<Void>> _cancelState = new MutableLiveData<>();
    public LiveData<Resource<Void>> cancelState = _cancelState;

    public BookingViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadMyBookings() {
        _bookingsState.setValue(Resource.loading(null));
        bookingRepository.getBookings(getApplication(), new RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> data) {
                _bookingsState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _bookingsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void createBooking(long fieldId, long timeSlotId, String bookingDate) {
        _bookingCreateState.setValue(Resource.loading(null));
        com.example.timsanbong.data.model.BookingRequest request = new com.example.timsanbong.data.model.BookingRequest();
        request.setFieldId(fieldId);
        request.setTimeSlotId(timeSlotId);
        request.setBookingDate(bookingDate);
        bookingRepository.createBooking(getApplication(), request,
                new RepositoryCallback<Booking>() {
                    @Override
                    public void onSuccess(Booking data) {
                        _bookingCreateState.postValue(Resource.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        _bookingCreateState.postValue(Resource.error(message, null));
                    }
                });
    }

    public void cancelBooking(long bookingId) {
        _cancelState.setValue(Resource.loading(null));
        bookingRepository.cancelBooking(getApplication(), bookingId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _cancelState.postValue(Resource.success(null));
                loadMyBookings();
            }

            @Override
            public void onError(String message) {
                _cancelState.postValue(Resource.error(message, null));
            }
        });
    }
}
