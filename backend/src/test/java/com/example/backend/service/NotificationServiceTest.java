package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.entity.User;
import com.example.backend.entity.UserFcmToken;
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
        when(userFcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.empty());

        notificationService.registerFcmToken(token);

        ArgumentCaptor<UserFcmToken> captor = ArgumentCaptor.forClass(UserFcmToken.class);
        verify(userFcmTokenRepository, times(1)).save(captor.capture());

        UserFcmToken savedToken = captor.getValue();
        assertEquals(1L, savedToken.getUserId());
        assertEquals(token, savedToken.getFcmToken());
        assertNotNull(savedToken.getCreatedAt());
    }

    @Test
    void testRegisterExistingFcmTokenDifferentUser() {
        String token = "sample-fcm-token-123";
        UserFcmToken existing = new UserFcmToken();
        existing.setId(10L);
        existing.setUserId(2L); // previously user 2
        existing.setFcmToken(token);
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(userFcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(existing));

        notificationService.registerFcmToken(token);

        verify(userFcmTokenRepository, times(1)).save(existing);
        assertEquals(1L, existing.getUserId()); // reassigned to user 1
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
