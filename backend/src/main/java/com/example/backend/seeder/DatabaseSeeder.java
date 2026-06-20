package com.example.backend.seeder;

import com.example.backend.entity.Field;
import com.example.backend.entity.TimeSlot;
import com.example.backend.entity.User;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.TimeSlotRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.utils.Enums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;
    private final jakarta.persistence.EntityManager entityManager;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.owner.email}")
    private String ownerEmail;

    @Value("${app.seed.owner.password}")
    private String ownerPassword;

    @Value("${app.seed.player.email}")
    private String playerEmail;

    @Value("${app.seed.player.password}")
    private String playerPassword;

    @Value("${app.seed.opponent.email}")
    private String opponentEmail;

    @Value("${app.seed.opponent.password}")
    private String opponentPassword;

    public DatabaseSeeder(UserRepository userRepository,
                          FieldRepository fieldRepository,
                          TimeSlotRepository timeSlotRepository,
                          PasswordEncoder passwordEncoder,
                          jakarta.persistence.EntityManager entityManager) {
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        validateSeedCredentials();

        // 0. Clean up any old fields (and their cascade references) that are not in Sân 1 - Sân 10
        log.info("Cleaning up old fields and associated records...");
        entityManager.createNativeQuery("DELETE FROM public.opponent_reviews WHERE match_id IN (SELECT id FROM public.match_posts WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10')))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.match_requests WHERE post_id IN (SELECT id FROM public.match_posts WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10')))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.match_posts WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10'))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.payments WHERE booking_id IN (SELECT id FROM public.bookings WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10')))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.field_reviews WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10'))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.bookings WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10'))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.time_slots WHERE field_id NOT IN (SELECT id FROM public.fields WHERE name IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10'))").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.fields WHERE name NOT IN ('Sân 1', 'Sân 2', 'Sân 3', 'Sân 4', 'Sân 5', 'Sân 6', 'Sân 7', 'Sân 8', 'Sân 9', 'Sân 10')").executeUpdate();

        // 1. Seed main users from environment variables
        User admin = ensureUser(adminEmail, adminPassword, Enums.UserRole.ADMIN, "SE114 Admin", "0901000000");
        User owner1 = ensureUser(ownerEmail, ownerPassword, Enums.UserRole.OWNER, "SE114 Owner", "0901000001");
        User player1 = ensureUser(playerEmail, playerPassword, Enums.UserRole.PLAYER, "SE114 Player 1", "0902000001");
        User player2 = ensureUser(opponentEmail, opponentPassword, Enums.UserRole.PLAYER, "SE114 Player 2", "0902000002");

        // 2. Seed additional players (total 10 player accounts including player1 and player2)
        User player3 = ensureUser("player3@example.com", "123456", Enums.UserRole.PLAYER, "Nguyễn Minh Triết", "0902000003");
        User player4 = ensureUser("player4@example.com", "123456", Enums.UserRole.PLAYER, "Trần Anh Tuấn", "0902000004");
        User player5 = ensureUser("player5@example.com", "123456", Enums.UserRole.PLAYER, "Lê Hoàng Nam", "0902000005");
        User player6 = ensureUser("player6@example.com", "123456", Enums.UserRole.PLAYER, "Phạm Đức Hải", "0902000006");
        User player7 = ensureUser("player7@example.com", "123456", Enums.UserRole.PLAYER, "Võ Minh Khang", "0902000007");
        User player8 = ensureUser("player8@example.com", "123456", Enums.UserRole.PLAYER, "Hoàng Anh Đức", "0902000008");
        User player9 = ensureUser("player9@example.com", "123456", Enums.UserRole.PLAYER, "Đỗ Gia Bảo", "0902000009");
        User player10 = ensureUser("player10@example.com", "123456", Enums.UserRole.PLAYER, "Bùi Tiến Dũng", "0902000010");

        // 3. Seed 10 Fields all owned by owner1
        Field f1 = ensureField(owner1, "Sân 1", "Sân bóng cỏ nhân tạo 7 người, mát mẻ, đèn chiếu sáng hiện đại.", Enums.FieldType.SEVEN_A_SIDE);
        Field f2 = ensureField(owner1, "Sân 2", "Sân bóng 5 người, chất lượng cỏ nhân tạo tốt.", Enums.FieldType.FIVE_A_SIDE);
        Field f3 = ensureField(owner1, "Sân 3", "Sân bóng 7 người tiêu chuẩn, mặt cỏ êm, có lưới chắn bóng cao.", Enums.FieldType.SEVEN_A_SIDE);
        Field f4 = ensureField(owner1, "Sân 4", "Sân bóng 5 người thoáng mát, không khí trong lành.", Enums.FieldType.FIVE_A_SIDE);
        Field f5 = ensureField(owner1, "Sân 5", "Sân bóng 5 người trung tâm cụm sân, có bãi giữ xe rộng rãi.", Enums.FieldType.FIVE_A_SIDE);
        Field f6 = ensureField(owner1, "Sân 6", "Sân bóng 7 người chất lượng cao, kích thước tiêu chuẩn.", Enums.FieldType.SEVEN_A_SIDE);
        Field f7 = ensureField(owner1, "Sân 7", "Sân bóng 5 người chất lượng, có khán đài và mái che một phần.", Enums.FieldType.FIVE_A_SIDE);
        Field f8 = ensureField(owner1, "Sân 8", "Sân bóng 7 người hiện đại, yên tĩnh.", Enums.FieldType.SEVEN_A_SIDE);
        Field f9 = ensureField(owner1, "Sân 9", "Sân bóng 5 người, mặt cỏ mới làm lại, hệ thống thoát nước cực tốt.", Enums.FieldType.FIVE_A_SIDE);
        Field f10 = ensureField(owner1, "Sân 10", "Sân bóng 5 người giá rẻ, phù hợp cho học sinh sinh viên.", Enums.FieldType.FIVE_A_SIDE);

        // 4. Seed Time Slots for all 10 Fields
        List.of(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10).forEach(this::ensureTimeSlots);

        log.info("Database Seeder completed successfully. Seeded 10+ users, 10 fields under 1 owner, 60 time slots.");
    }

    private void validateSeedCredentials() {
        requireText(adminEmail, "APP_SEED_ADMIN_EMAIL");
        requireText(adminPassword, "APP_SEED_ADMIN_PASSWORD");
        requireText(ownerEmail, "APP_SEED_OWNER_EMAIL");
        requireText(ownerPassword, "APP_SEED_OWNER_PASSWORD");
        requireText(playerEmail, "APP_SEED_PLAYER_EMAIL");
        requireText(playerPassword, "APP_SEED_PLAYER_PASSWORD");
        requireText(opponentEmail, "APP_SEED_OPPONENT_EMAIL");
        requireText(opponentPassword, "APP_SEED_OPPONENT_PASSWORD");
    }

    private void requireText(String value, String envName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(envName + " must be configured when app.seed.enabled=true");
        }
    }

    private User ensureUser(String email, String password, Enums.UserRole role, String fullName, String phone) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email.trim().toLowerCase());
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRole(role);
                    user.setFullName(fullName);
                    user.setPhone(phone);
                    user.setTrustScore(100);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    private Field ensureField(User owner, String name, String description, Enums.FieldType type) {
        return fieldRepository.findAll().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(existingField -> {
                    if (!owner.getId().equals(existingField.getOwnerId())) {
                        existingField.setOwnerId(owner.getId());
                        return fieldRepository.save(existingField);
                    }
                    return existingField;
                })
                .orElseGet(() -> {
                    Field field = new Field();
                    field.setOwnerId(owner.getId());
                    field.setName(name);
                    field.setDescription(description);
                    field.setType(type);
                    field.setStatus(Enums.FieldStatus.AVAILABLE);
                    field.setCoverImage(null);
                    field.setCreatedAt(LocalDateTime.now());
                    field.setUpdatedAt(LocalDateTime.now());
                    return fieldRepository.save(field);
                });
    }

    private void ensureTimeSlots(Field field) {
        if (timeSlotRepository.findByFieldIdOrderByStartTimeAsc(field.getId()).size() >= 11) {
            return;
        }

        // Just in case, delete existing so we don't have duplicates
        entityManager.createNativeQuery("DELETE FROM public.bookings WHERE time_slot_id IN (SELECT id FROM public.time_slots WHERE field_id = :fieldId)")
            .setParameter("fieldId", field.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM public.time_slots WHERE field_id = :fieldId")
            .setParameter("fieldId", field.getId())
            .executeUpdate();

        List<TimeSlot> timeSlots = List.of(
                buildTimeSlot(field.getId(), LocalTime.of(6, 0), LocalTime.of(7, 30), BigDecimal.valueOf(180000)),
                buildTimeSlot(field.getId(), LocalTime.of(7, 30), LocalTime.of(9, 0), BigDecimal.valueOf(180000)),
                buildTimeSlot(field.getId(), LocalTime.of(9, 0), LocalTime.of(10, 30), BigDecimal.valueOf(180000)),
                buildTimeSlot(field.getId(), LocalTime.of(10, 30), LocalTime.of(12, 0), BigDecimal.valueOf(200000)),
                buildTimeSlot(field.getId(), LocalTime.of(12, 0), LocalTime.of(13, 30), BigDecimal.valueOf(200000)),
                buildTimeSlot(field.getId(), LocalTime.of(13, 30), LocalTime.of(15, 0), BigDecimal.valueOf(200000)),
                buildTimeSlot(field.getId(), LocalTime.of(15, 0), LocalTime.of(16, 30), BigDecimal.valueOf(250000)),
                buildTimeSlot(field.getId(), LocalTime.of(16, 30), LocalTime.of(18, 0), BigDecimal.valueOf(250000)),
                buildTimeSlot(field.getId(), LocalTime.of(18, 0), LocalTime.of(19, 30), BigDecimal.valueOf(350000)),
                buildTimeSlot(field.getId(), LocalTime.of(19, 30), LocalTime.of(21, 0), BigDecimal.valueOf(350000)),
                buildTimeSlot(field.getId(), LocalTime.of(21, 0), LocalTime.of(22, 30), BigDecimal.valueOf(200000))
        );
        timeSlotRepository.saveAll(timeSlots);
    }

    private TimeSlot buildTimeSlot(Long fieldId, LocalTime startTime, LocalTime endTime, BigDecimal price) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setFieldId(fieldId);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setPrice(price);
        timeSlot.setStatus(Enums.TimeSlotStatus.AVAILABLE);
        return timeSlot;
    }


}
