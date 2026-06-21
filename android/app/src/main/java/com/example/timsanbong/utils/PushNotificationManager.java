package com.example.timsanbong.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.timsanbong.data.api.ApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PushNotificationManager {
    public static final String CHANNEL_ID = "soccer_booking_notifications";
    public static final int REQUEST_POST_NOTIFICATIONS = 101;

    private static final String TAG = "PushNotifications";
    private static final String PREFS_PUSH = "push_notifications";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private PushNotificationManager() {
    }

    public static void prepareForAuthenticatedUser(Activity activity) {
        createDefaultNotificationChannel(activity);
        requestPostNotificationsPermission(activity);
        syncToken(activity);
    }

    public static void requestPostNotificationsPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_POST_NOTIFICATIONS
        );
    }

    public static boolean canPostNotifications(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void createDefaultNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Timsanbong notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Booking, matchmaking, and system notification updates.");
        manager.createNotificationChannel(channel);
    }

    public static void syncToken(Context context) {
        Context appContext = context.getApplicationContext();
        if (!new SessionManager(appContext).isLoggedIn() || !ensureFirebaseReady(appContext)) {
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().trim().isEmpty()) {
                Log.w(TAG, "Unable to load FCM token", task.getException());
                return;
            }

            handleNewToken(appContext, task.getResult());
        });
    }

    public static void handleNewToken(Context context, String token) {
        Context appContext = context.getApplicationContext();
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        saveToken(appContext, token);
        if (!new SessionManager(appContext).isLoggedIn()) {
            return;
        }

        ApiClient.getService(appContext).registerFcmToken(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "FCM token registration failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "FCM token registration failed", t);
            }
        });
    }

    public static void deregisterCurrentToken(Context context, Runnable onComplete) {
        Context appContext = context.getApplicationContext();
        String savedToken = getSavedToken(appContext);
        if (savedToken != null && !savedToken.trim().isEmpty()) {
            deregisterToken(appContext, savedToken, onComplete);
            return;
        }

        if (!ensureFirebaseReady(appContext)) {
            runOnMain(onComplete);
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().trim().isEmpty()) {
                deregisterToken(appContext, task.getResult(), onComplete);
            } else {
                Log.w(TAG, "Unable to load FCM token for logout", task.getException());
                runOnMain(onComplete);
            }
        });
    }

    private static void deregisterToken(Context context, String token, Runnable onComplete) {
        ApiClient.getService(context).deregisterFcmToken(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "FCM token deregistration failed: " + response.code());
                }
                clearSavedToken(context);
                runOnMain(onComplete);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "FCM token deregistration failed", t);
                runOnMain(onComplete);
            }
        });
    }

    private static boolean ensureFirebaseReady(Context context) {
        try {
            if (!FirebaseApp.getApps(context).isEmpty()) {
                return true;
            }
            return FirebaseApp.initializeApp(context) != null;
        } catch (IllegalStateException e) {
            Log.w(TAG, "Firebase is not configured yet", e);
            return false;
        }
    }

    private static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    private static String getSavedToken(Context context) {
        return getPrefs(context).getString(KEY_FCM_TOKEN, null);
    }

    private static void clearSavedToken(Context context) {
        getPrefs(context).edit().remove(KEY_FCM_TOKEN).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_PUSH, Context.MODE_PRIVATE);
    }

    private static void runOnMain(Runnable runnable) {
        if (runnable == null) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
