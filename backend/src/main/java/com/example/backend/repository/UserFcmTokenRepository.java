package com.example.backend.repository;

import com.example.backend.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    List<UserFcmToken> findByUserId(Long userId);

    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);

    @Modifying
    @Query(value = "INSERT INTO user_fcm_tokens (user_id, fcm_token, created_at) " +
            "VALUES (:userId, :fcmToken, :createdAt) " +
            "ON CONFLICT (fcm_token) DO UPDATE " +
            "SET user_id = EXCLUDED.user_id, " +
            "    created_at = EXCLUDED.created_at " +
            "WHERE user_fcm_tokens.user_id != EXCLUDED.user_id", nativeQuery = true)
    void upsert(@Param("userId") Long userId, @Param("fcmToken") String fcmToken, @Param("createdAt") LocalDateTime createdAt);
}
