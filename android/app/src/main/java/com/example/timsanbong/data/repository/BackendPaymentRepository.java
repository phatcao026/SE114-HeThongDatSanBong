package com.example.timsanbong.data.repository;

import android.content.Context;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.PaymentRequest;
import com.example.timsanbong.data.model.PaymentResponse;

import java.util.List;

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

    @Override
    public void getMyPayments(HistoryCallback callback) {
        ApiClient.getService(context).getMyPayments()
                .enqueue(new retrofit2.Callback<List<PaymentResponse>>() {
                    @Override
                    public void onResponse(Call<List<PaymentResponse>> call,
                                           Response<List<PaymentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Khong the tai lich su thanh toan.");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PaymentResponse>> call, Throwable t) {
                        callback.onError("Khong the ket noi may chu.");
                    }
                });
    }

    @Override
    public void getBookingPayments(long bookingId, HistoryCallback callback) {
        ApiClient.getService(context).getBookingPayments(bookingId)
                .enqueue(new retrofit2.Callback<List<PaymentResponse>>() {
                    @Override
                    public void onResponse(Call<List<PaymentResponse>> call,
                                           Response<List<PaymentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Khong the tai thanh toan cua don dat san.");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PaymentResponse>> call, Throwable t) {
                        callback.onError("Khong the ket noi may chu.");
                    }
                });
    }
}
