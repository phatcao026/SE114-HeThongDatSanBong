package com.example.backend.service.impl;

import com.example.backend.entity.UserFcmToken;
import com.example.backend.repository.UserFcmTokenRepository;
import com.example.backend.service.FcmPushService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.Notification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FcmPushServiceImpl implements FcmPushService {

    private final UserFcmTokenRepository userFcmTokenRepository;

    public FcmPushServiceImpl(UserFcmTokenRepository userFcmTokenRepository) {
        this.userFcmTokenRepository = userFcmTokenRepository;
    }

    @Override
    @Async
    @Transactional
    public void sendPushNotification(Long userId, String title, String content) {
        List<UserFcmToken> tokens = userFcmTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            return;
        }

        List<String> registrationTokens = tokens.stream()
                .map(UserFcmToken::getFcmToken)
                .toList();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(content)
                        .build())
                .addAllTokens(registrationTokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        com.google.firebase.messaging.FirebaseMessagingException exception = responses.get(i).getException();
                        if (exception != null && 
                            exception.getMessagingErrorCode() != null &&
                            ("UNREGISTERED".equals(exception.getMessagingErrorCode().name()) ||
                             "INVALID_ARGUMENT".equals(exception.getMessagingErrorCode().name()))) {
                            String badToken = registrationTokens.get(i);
                            userFcmTokenRepository.deleteByFcmToken(badToken);
                            System.out.println("Removed invalid FCM token: " + badToken);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending FCM notification: " + e.getMessage());
        }
    }
}
