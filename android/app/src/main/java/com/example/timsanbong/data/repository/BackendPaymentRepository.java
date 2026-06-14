package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.data.model.PaymentResponse;

import retrofit2.Call;
import retrofit2.Response;

public class BackendPaymentRepository implements PaymentRepository {

    private final Context context;

    public BackendPaymentRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void processPayment(PaymentRequest request, PaymentRepository.Callback callback) {
        ApiClient.getService(context).createCheckoutSession(request.getBookingId())
                .enqueue(new retrofit2.Callback<PaymentResponse>() {
                    @Override
                    public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentResponse paymentResponse = response.body();
                            callback.onSuccess(paymentResponse.getMessage(), paymentResponse.getUrl());
                        } else {
                            callback.onError("Khong the tao phien thanh toan.");
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentResponse> call, Throwable t) {
                        callback.onError("Khong the ket noi may chu.");
                    }
                });
    }
}
