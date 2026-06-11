package com.example.backend.service.impl;

import com.example.backend.dto.request.NotificationCreateRequest;
import com.example.backend.dto.response.NotificationResponse;
import com.example.backend.entity.Notification;
import com.example.backend.exception.AppException;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Boolean isRead) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        List<Notification> notifications = isRead == null
                ? notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                : notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(currentUserId, isRead);

        return notifications.stream().map(this::toResponse).toList();
    }

    @Override
    public long countUnread() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsRead(currentUserId, false);
    }

    @Override
    @Transactional
    public NotificationResponse createAdminNotification(NotificationCreateRequest request) {
        ensureAdminRole();
        return createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getContent(),
                request.getType()
        );
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(Long userId,
                                                   String title,
                                                   String content,
                                                   Enums.NotificationType type) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(404, "Notification user not found"));

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(cleanRequired(title, "Notification title is required"));
        notification.setContent(cleanRequired(content, "Notification content is required"));
        notification.setType(type != null ? type : Enums.NotificationType.SYSTEM);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = findOwnedNotification(id);
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public List<NotificationResponse> markAllAsRead() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(currentUserId, false);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        return notificationRepository.saveAll(unreadNotifications)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse deleteNotification(Long id) {
        Notification notification = findOwnedNotification(id);
        NotificationResponse response = toResponse(notification);
        notificationRepository.delete(notification);
        return response;
    }

    @Override
    @Transactional
    public void deleteReadNotifications() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        List<Notification> readNotifications =
                notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(currentUserId, true);
        notificationRepository.deleteAll(readNotifications);
    }

    private Notification findOwnedNotification(Long id) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return notificationRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new AppException(404, "Notification not found"));
    }

    private void ensureAdminRole() {
        if (!TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Only admins can create notifications");
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setType(notification.getType());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private String cleanRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(400, message);
        }

        return value.trim();
    }
}
