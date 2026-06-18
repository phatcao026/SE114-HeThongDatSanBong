package com.example.backend.service;

public interface FcmPushService {
    void sendPushNotification(Long userId, String title, String content);
}
