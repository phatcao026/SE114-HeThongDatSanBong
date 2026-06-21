package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.entity.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserFcmTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.NotificationServiceImpl;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private UserFcmTokenRepository userFcmTokenRepository;
    private FcmPushService fcmPushService;
    private NotificationService notificationService;

    private MockedStatic<TokenUtils> mockedTokenUtils;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        userRepository = mock(UserRepository.class);
        userFcmTokenRepository = mock(UserFcmTokenRepository.class);
        fcmPushService = mock(FcmPushService.class);

        notificationService = new NotificationServiceImpl(
                notificationRepository,
                userRepository,
                userFcmTokenRepository,
                fcmPushService
        );

        mockedTokenUtils = mockStatic(TokenUtils.class);
        mockedTokenUtils.when(TokenUtils::getCurrentUserId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        mockedTokenUtils.close();
    }

    @Test
    void testRegisterNewFcmToken() {
        String token = "sample-fcm-token-123";

        notificationService.registerFcmToken(token);

        verify(userFcmTokenRepository, times(1)).upsert(eq(1L), eq(token), any(LocalDateTime.class));
    }

    @Test
    void testRegisterExistingFcmTokenDifferentUser() {
        String token = "sample-fcm-token-123";

        notificationService.registerFcmToken(token);

        verify(userFcmTokenRepository, times(1)).upsert(eq(1L), eq(token), any(LocalDateTime.class));
    }

    @Test
    void testDeregisterFcmToken() {
        String token = "sample-fcm-token-123";
        notificationService.deregisterFcmToken(token);
        verify(userFcmTokenRepository, times(1)).deleteByFcmToken(token);
    }

    @Test
    void testCreateNotificationTriggersFcmPush() {
        Long userId = 1L;
        String title = "Test Push Title";
        String content = "Test Push Content";
        Enums.NotificationType type = Enums.NotificationType.SYSTEM;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createNotification(userId, title, content, type);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(fcmPushService, times(1)).sendPushNotification(userId, title, content);
    }
}
