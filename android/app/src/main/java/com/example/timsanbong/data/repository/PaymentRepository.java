package com.example.timsanbong.data.repository;

import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.data.model.PaymentResponse;

import java.util.List;

public interface PaymentRepository {
    interface Callback {
        void onSuccess(PaymentResponse payment);
        void onError(String message);
    }

    interface HistoryCallback {
        void onSuccess(List<PaymentResponse> payments);
        void onError(String message);
    }

    void processPayment(PaymentRequest request, Callback callback);
    void verifyCheckoutSession(String sessionId, Callback callback);
    void getMyPayments(HistoryCallback callback);
    void getBookingPayments(long bookingId, HistoryCallback callback);
}
