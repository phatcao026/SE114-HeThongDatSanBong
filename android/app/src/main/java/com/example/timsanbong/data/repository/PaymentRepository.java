package com.example.timsanbong.data.repository;

import com.example.timsanbong.data.model.PaymentRequest;

public interface PaymentRepository {
    interface Callback {
        void onSuccess(String message);
        void onError(String message);
    }

    void processPayment(PaymentRequest request, Callback callback);
}

