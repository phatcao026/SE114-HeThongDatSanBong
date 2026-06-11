package com.example.backend.scheduler;

import com.example.backend.entity.Booking;
import com.example.backend.repository.BookingRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.utils.Enums;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.booking.cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class BookingCleanupTask {
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Value("${app.booking.pending-timeout-minutes:15}")
    private long pendingTimeoutMinutes;

    public BookingCleanupTask(BookingRepository bookingRepository,
                              NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(
            initialDelayString = "${app.booking.cleanup-initial-delay-ms:30000}",
            fixedDelayString = "${app.booking.cleanup-fixed-delay-ms:60000}"
    )
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(pendingTimeoutMinutes);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                Enums.BookingStatus.PENDING,
                deadline
        );

        for (Booking booking : expiredBookings) {
            booking.setStatus(Enums.BookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            notifyBookingCancelled(booking);
        }

        bookingRepository.saveAll(expiredBookings);
    }

    private void notifyBookingCancelled(Booking booking) {
        if (booking.getUserId() == null) {
            return;
        }

        notificationService.createNotification(
                booking.getUserId(),
                "Booking expired",
                "Your booking #" + booking.getId() + " was cancelled because payment was not completed in time.",
                Enums.NotificationType.BOOKING_UPDATE
        );
    }
}
