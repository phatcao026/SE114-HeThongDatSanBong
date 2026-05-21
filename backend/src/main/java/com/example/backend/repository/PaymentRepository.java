package com.example.backend.repository;

import com.example.backend.entity.Payment;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Payment> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(Long bookingId, Enums.PaymentStatus status);

    boolean existsByBookingIdAndStatus(Long bookingId, Enums.PaymentStatus status);
}
