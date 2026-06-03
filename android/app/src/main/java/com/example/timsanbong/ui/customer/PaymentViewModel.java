package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.data.repository.MockPaymentRepository;
import com.example.timsanbong.data.repository.PaymentRepository;
import com.example.timsanbong.utils.Constants;

public class PaymentViewModel extends AndroidViewModel {

    public enum Status {
        INIT, PROCESSING, SUCCESS, FAILED
    }

    public static class PaymentState {
        public final Status status;
        public final String message;

        public PaymentState(Status status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    private final MutableLiveData<PaymentState> _paymentState = new MutableLiveData<>();
    public LiveData<PaymentState> paymentState = _paymentState;

    private final PaymentRepository repository;

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        if (Constants.MOCK_PAYMENT_MODE) {
            repository = new MockPaymentRepository();
        } else {
            repository = new MockPaymentRepository();
        }
        _paymentState.setValue(new PaymentState(Status.INIT, null));
    }

    public void startPayment(PaymentRequest request) {
        _paymentState.setValue(new PaymentState(Status.PROCESSING, null));
        repository.processPayment(request, new PaymentRepository.Callback() {
            @Override
            public void onSuccess(String message) {
                _paymentState.postValue(new PaymentState(Status.SUCCESS, message));
            }

            @Override
            public void onError(String message) {
                _paymentState.postValue(new PaymentState(Status.FAILED, message));
            }
        });
    }

    public void reset() {
        _paymentState.setValue(new PaymentState(Status.INIT, null));
    }
}

