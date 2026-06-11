package com.example.backend.controller;

import com.example.backend.dto.request.NotificationCreateRequest;
import com.example.backend.dto.response.NotificationResponse;
import com.example.backend.dto.response.UnreadNotificationCountResponse;
import com.example.backend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false) Boolean isRead) {
        return ResponseEntity.ok(notificationService.getMyNotifications(isRead));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> countUnread() {
        return ResponseEntity.ok(new UnreadNotificationCountResponse(notificationService.countUnread()));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createAdminNotification(
            @Valid @RequestBody NotificationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createAdminNotification(request));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<List<NotificationResponse>> markAllAsRead() {
        return ResponseEntity.ok(notificationService.markAllAsRead());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<NotificationResponse> deleteNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.deleteNotification(id));
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteReadNotifications() {
        notificationService.deleteReadNotifications();
        return ResponseEntity.noContent().build();
    }
}
