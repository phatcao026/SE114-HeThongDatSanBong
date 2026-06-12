package com.example.timsanbong.data.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import okhttp3.Authenticator;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.example.timsanbong.ui.auth.LoginActivity;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.SessionManager;

public class ApiClient {
    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;

    public static OkHttpClient getRawClient(Context context) {
        if (okHttpClient == null) {
            initClient(context);
        }
        return okHttpClient;
    }

    public static ApiService getService(Context context) {
        if (retrofit == null) {
            initClient(context);
        }
        return retrofit.create(ApiService.class);
    }

    private static void initClient(Context context) {
            SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Authenticator authenticator = new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) {
                    if (response.code() == 401) {
                        SessionManager sessionManager = new SessionManager(context);
                        sessionManager.clearSession();
                        
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Intent intent = new Intent(context, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                        });
                    }
                    return null;
                }
            };

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = prefs.getString("token", null);
                        Request original = chain.request();
                        if (token != null) {
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(request);
                        }
                        return chain.proceed(original);
                    })
                    .authenticator(authenticator)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

    public static void reset() {
        retrofit = null;
    }
}
