package com.example.backend.service;

import com.example.backend.dto.request.NotificationCreateRequest;
import com.example.backend.dto.response.NotificationResponse;
import com.example.backend.utils.Enums;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications(Boolean isRead);

    long countUnread();

    NotificationResponse createAdminNotification(NotificationCreateRequest request);

    NotificationResponse createNotification(Long userId, String title, String content, Enums.NotificationType type);

    NotificationResponse markAsRead(Long id);

    List<NotificationResponse> markAllAsRead();

    NotificationResponse deleteNotification(Long id);

    void deleteReadNotifications();

    void registerFcmToken(String fcmToken);

    void deregisterFcmToken(String fcmToken);
}
