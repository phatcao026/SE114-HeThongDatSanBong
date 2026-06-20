package com.example.timsanbong.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.timsanbong.R;
import com.example.timsanbong.ui.auth.SplashActivity;
import com.example.timsanbong.utils.PushNotificationManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class TimSanBongFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        PushNotificationManager.handleNewToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        PushNotificationManager.createDefaultNotificationChannel(this);
        if (!PushNotificationManager.canPostNotifications(this)) {
            return;
        }

        String title = getMessageTitle(remoteMessage);
        String body = getMessageBody(remoteMessage);
        showNotification(title, body);
    }

    private String getMessageTitle(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null
                && remoteMessage.getNotification().getTitle() != null
                && !remoteMessage.getNotification().getTitle().trim().isEmpty()) {
            return remoteMessage.getNotification().getTitle();
        }

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        return title == null || title.trim().isEmpty() ? getString(R.string.app_name) : title;
    }

    private String getMessageBody(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null
                && remoteMessage.getNotification().getBody() != null
                && !remoteMessage.getNotification().getBody().trim().isEmpty()) {
            return remoteMessage.getNotification().getBody();
        }

        Map<String, String> data = remoteMessage.getData();
        String body = data.get("body");
        if (body == null || body.trim().isEmpty()) {
            body = data.get("content");
        }
        return body == null || body.trim().isEmpty()
                ? "You have a new TimSanBong update."
                : body;
    }

    private void showNotification(String title, String body) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                PushNotificationManager.CHANNEL_ID
        )
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
