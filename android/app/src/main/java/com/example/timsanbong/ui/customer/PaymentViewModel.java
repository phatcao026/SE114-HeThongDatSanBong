package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.data.model.PaymentResponse;
import com.example.timsanbong.data.repository.BackendPaymentRepository;
import com.example.timsanbong.data.repository.PaymentRepository;

import java.util.List;
import java.util.Locale;

public class PaymentViewModel extends AndroidViewModel {

    public enum Status {
        INIT, PROCESSING, CHECKOUT_READY, VERIFYING, PAID, CANCELLED, PENDING, FAILED
    }

    public static class PaymentState {
        public final Status status;
        public final String message;
        public final String checkoutUrl;
        public final String checkoutSessionId;
        public final PaymentResponse payment;

        public PaymentState(Status status, String message) {
            this(status, message, null, null, null);
        }

        public PaymentState(Status status, String message, String checkoutUrl,
                            String checkoutSessionId, PaymentResponse payment) {
            this.status = status;
            this.message = message;
            this.checkoutUrl = checkoutUrl;
            this.checkoutSessionId = checkoutSessionId;
            this.payment = payment;
        }
    }

    private final MutableLiveData<PaymentState> _paymentState = new MutableLiveData<>();
    public LiveData<PaymentState> paymentState = _paymentState;

    private final PaymentRepository repository;

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        repository = new BackendPaymentRepository(application);
        _paymentState.setValue(new PaymentState(Status.INIT, null));
    }

    public void startPayment(PaymentRequest request) {
        _paymentState.setValue(new PaymentState(Status.PROCESSING, null));
        repository.processPayment(request, new PaymentRepository.Callback() {
            @Override
            public void onSuccess(PaymentResponse payment) {
                _paymentState.postValue(toCheckoutReadyState(payment));
            }

            @Override
            public void onError(String message) {
                _paymentState.postValue(new PaymentState(Status.FAILED, message));
            }
        });
    }

    public void refreshBookingPayment(long bookingId) {
        if (bookingId <= 0) {
            _paymentState.setValue(new PaymentState(Status.FAILED, "Khong tim thay don dat san."));
            return;
        }

        _paymentState.setValue(new PaymentState(Status.VERIFYING, null));
        repository.getBookingPayments(bookingId, new PaymentRepository.HistoryCallback() {
            @Override
            public void onSuccess(List<PaymentResponse> payments) {
                _paymentState.postValue(toHistoryState(payments));
            }

            @Override
            public void onError(String message) {
                _paymentState.postValue(new PaymentState(Status.FAILED, message));
            }
        });
    }

    public void verifyCheckoutSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            _paymentState.setValue(new PaymentState(Status.FAILED, "Khong tim thay phien thanh toan."));
            return;
        }

        _paymentState.setValue(new PaymentState(Status.VERIFYING, null));
        repository.verifyCheckoutSession(sessionId.trim(), new PaymentRepository.Callback() {
            @Override
            public void onSuccess(PaymentResponse payment) {
                _paymentState.postValue(toVerifiedState(payment));
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

    private PaymentState toCheckoutReadyState(PaymentResponse payment) {
        return new PaymentState(
                Status.CHECKOUT_READY,
                payment != null ? payment.getMessage() : null,
                payment != null ? payment.getUrl() : null,
                payment != null ? payment.getStripePaymentIntentId() : null,
                payment
        );
    }

    private PaymentState toHistoryState(List<PaymentResponse> payments) {
        if (payments == null || payments.isEmpty()) {
            return new PaymentState(Status.PENDING, "Thanh toan dang cho xac nhan.");
        }

        PaymentResponse latestPending = null;
        PaymentResponse latestFailure = null;

        for (PaymentResponse payment : payments) {
            String status = normalizeStatus(payment);
            if ("SUCCESS".equals(status)) {
                return toVerifiedState(payment);
            }
            if (latestFailure == null && ("FAILED".equals(status) || "REFUNDED".equals(status))) {
                latestFailure = payment;
            }
            if (latestPending == null && "PENDING".equals(status)) {
                latestPending = payment;
            }
        }

        if (latestFailure != null) {
            return toVerifiedState(latestFailure);
        }
        if (latestPending != null) {
            return toVerifiedState(latestPending);
        }
        return new PaymentState(Status.PENDING, "Thanh toan dang cho xac nhan.", null, null, payments.get(0));
    }

    private PaymentState toVerifiedState(PaymentResponse payment) {
        String status = normalizeStatus(payment);
        Status nextStatus;
        if ("SUCCESS".equals(status)) {
            nextStatus = Status.PAID;
        } else if ("FAILED".equals(status) || "REFUNDED".equals(status)) {
            nextStatus = Status.CANCELLED;
        } else if ("PENDING".equals(status)) {
            nextStatus = Status.PENDING;
        } else {
            nextStatus = Status.FAILED;
        }

        return new PaymentState(
                nextStatus,
                payment != null ? payment.getMessage() : null,
                payment != null ? payment.getUrl() : null,
                payment != null ? payment.getStripePaymentIntentId() : null,
                payment
        );
    }

    private String normalizeStatus(PaymentResponse payment) {
        String status = payment != null && payment.getStatus() != null
                ? payment.getStatus().toUpperCase(Locale.US)
                : "";
        return status;
    }
}
