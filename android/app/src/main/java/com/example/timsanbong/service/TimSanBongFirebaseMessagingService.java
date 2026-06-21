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
        showNotification(title, body, remoteMessage.getData());
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
                ? "You have a new Timsanbong update."
                : body;
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        Intent intent;
        String type = data.get("type");
        if ("CHAT".equals(type) && data.containsKey("conversationId")) {
            intent = new Intent(this, com.example.timsanbong.ui.customer.MessagesActivity.class);
            // Optionally pass ID if MessagesActivity can auto-open it
            intent.putExtra("conversationId", data.get("conversationId"));
        } else {
            intent = new Intent(this, SplashActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, flags);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                PushNotificationManager.CHANNEL_ID
        )
                .setSmallIcon(R.drawable.ic_message)
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
