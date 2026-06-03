package com.example.timsanbong.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.utils.Constants;

public class MockPaymentRepository implements PaymentRepository {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void processPayment(PaymentRequest request, Callback callback) {
        handler.postDelayed(() -> {
            if (Constants.MOCK_PAYMENT_SUCCESS) {
                callback.onSuccess("Thanh toán thành công.");
            } else {
                callback.onError("Thanh toán thất bại. Vui lòng thử lại.");
            }
        }, 1200);
    }
}

