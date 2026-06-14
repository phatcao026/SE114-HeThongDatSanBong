package com.example.backend.seeder;

import com.example.backend.entity.Field;
import com.example.backend.entity.Team;
import com.example.backend.entity.TimeSlot;
import com.example.backend.entity.User;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.TeamRepository;
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
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

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
                          TeamRepository teamRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        validateSeedCredentials();

        // 1. Seed main users from environment variables
        User admin = ensureUser(adminEmail, adminPassword, Enums.UserRole.ADMIN, "SE114 Admin", "0901000000");
        User owner1 = ensureUser(ownerEmail, ownerPassword, Enums.UserRole.OWNER, "SE114 Owner 1", "0901000001");
        User player1 = ensureUser(playerEmail, playerPassword, Enums.UserRole.PLAYER, "SE114 Player 1", "0902000001");
        User player2 = ensureUser(opponentEmail, opponentPassword, Enums.UserRole.PLAYER, "SE114 Player 2", "0902000002");

        // 2. Seed additional owners programmatically (total 3 owners)
        User owner2 = ensureUser("owner2@example.com", "123456", Enums.UserRole.OWNER, "Chủ sân Nguyễn Văn A", "0901000002");
        User owner3 = ensureUser("owner3@example.com", "123456", Enums.UserRole.OWNER, "Chủ sân Trần Thị B", "0901000003");

        // 3. Seed additional players (total 10 player accounts including player1 and player2)
        User player3 = ensureUser("player3@example.com", "123456", Enums.UserRole.PLAYER, "Nguyễn Minh Triết", "0902000003");
        User player4 = ensureUser("player4@example.com", "123456", Enums.UserRole.PLAYER, "Trần Anh Tuấn", "0902000004");
        User player5 = ensureUser("player5@example.com", "123456", Enums.UserRole.PLAYER, "Lê Hoàng Nam", "0902000005");
        User player6 = ensureUser("player6@example.com", "123456", Enums.UserRole.PLAYER, "Phạm Đức Hải", "0902000006");
        User player7 = ensureUser("player7@example.com", "123456", Enums.UserRole.PLAYER, "Võ Minh Khang", "0902000007");
        User player8 = ensureUser("player8@example.com", "123456", Enums.UserRole.PLAYER, "Hoàng Anh Đức", "0902000008");
        User player9 = ensureUser("player9@example.com", "123456", Enums.UserRole.PLAYER, "Đỗ Gia Bảo", "0902000009");
        User player10 = ensureUser("player10@example.com", "123456", Enums.UserRole.PLAYER, "Bùi Tiến Dũng", "0902000010");

        // 4. Seed 10 Fields across the 3 owners
        Field f1 = ensureField(owner1, "Sân Bóng Thống Nhất", "138 Đào Duy Từ, Phường 6, Quận 10, TP.HCM", "Sân bóng cỏ nhân tạo mát mẻ, đèn chiếu sáng hiện đại.", Enums.FieldType.SEVEN_A_SIDE);
        Field f2 = ensureField(owner1, "Sân Bóng Hoa Lư", "2 Đinh Tiên Hoàng, Đa Kao, Quận 1, TP.HCM", "Sân trung tâm quận 1, thuận tiện đi lại, dịch vụ tốt.", Enums.FieldType.FIVE_A_SIDE);
        Field f3 = ensureField(owner1, "Sân Bóng Kỳ Hòa", "824 Sư Vạn Hạnh, Phường 12, Quận 10, TP.HCM", "Sân 7 người tiêu chuẩn, mặt cỏ êm, có lưới chắn bóng cao.", Enums.FieldType.SEVEN_A_SIDE);
        Field f4 = ensureField(owner1, "Sân Bóng Khánh Hội", "Đường số 48, Phường 5, Quận 4, TP.HCM", "Nằm trong công viên Khánh Hội, không khí trong lành, mát mẻ.", Enums.FieldType.FIVE_A_SIDE);

        Field f5 = ensureField(owner2, "Sân Bóng Tao Đàn", "1 Huyền Trân Công Chúa, Bến Thành, Quận 1, TP.HCM", "Sân cỏ nhân tạo trung tâm thành phố, có bãi giữ xe rộng rãi.", Enums.FieldType.FIVE_A_SIDE);
        Field f6 = ensureField(owner2, "Sân Bóng Phú Thọ", "2 Lữ Gia, Phường 15, Quận 11, TP.HCM", "Sân 11 người kích thước tiêu chuẩn thi đấu quốc tế.", Enums.FieldType.ELEVEN_A_SIDE);
        Field f7 = ensureField(owner2, "Sân Bóng Rạch Miễu", "1 Hoa Phượng, Phường 2, Phú Nhuận, TP.HCM", "Sân bóng chất lượng, có khán đài và mái che một phần.", Enums.FieldType.FIVE_A_SIDE);

        Field f8 = ensureField(owner3, "Sân Bóng Celadon City", "Đường D2, Sơn Kỳ, Tân Phu, TP.HCM", "Nằm trong khu đô thị Celadon, yên tĩnh và hiện đại.", Enums.FieldType.SEVEN_A_SIDE);
        Field f9 = ensureField(owner3, "Sân Bóng Bình Thạnh", "324 Chu Văn An, Phường 12, Bình Thạnh, TP.HCM", "Mặt cỏ mới làm lại, hệ thống thoát nước cực tốt.", Enums.FieldType.FIVE_A_SIDE);
        Field f10 = ensureField(owner3, "Sân Bóng Cát Lái", "Đường 35, Cát Lái, Quận 2, TP.HCM", "Sân bóng giá rẻ, phù hợp cho học sinh sinh viên.", Enums.FieldType.FIVE_A_SIDE);

        // 5. Seed Time Slots for all 10 Fields
        List.of(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10).forEach(this::ensureTimeSlots);

        // 6. Seed Teams for some players (total 5 teams)
        ensureTeam(player1, "SE114 Demo Team", "Đội bóng thử nghiệm của SE114", Enums.TeamLevel.BEGINNER);
        ensureTeam(player2, "FC Dragon", "Đội bóng của các chiến binh rồng bay", Enums.TeamLevel.INTERMEDIATE);
        ensureTeam(player3, "FC Brotherhood", "Tinh thần anh em chiến hữu là trên hết", Enums.TeamLevel.ADVANCED);
        ensureTeam(player4, "FC Young Boys", "Đội tuyển trẻ nhiệt huyết năng động", Enums.TeamLevel.BEGINNER);
        ensureTeam(player5, "FC Golden Star", "Đội bóng hướng tới các ngôi sao vàng", Enums.TeamLevel.INTERMEDIATE);

        log.info("Database Seeder completed successfully. Seeded 10+ users, 10 fields, 60 time slots, and 5 teams.");
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

    private Field ensureField(User owner, String name, String address, String description, Enums.FieldType type) {
        return fieldRepository.findAll().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Field field = new Field();
                    field.setOwnerId(owner.getId());
                    field.setName(name);
                    field.setAddress(address);
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
        if (!timeSlotRepository.findByFieldIdOrderByStartTimeAsc(field.getId()).isEmpty()) {
            return;
        }

        List<TimeSlot> timeSlots = List.of(
                buildTimeSlot(field.getId(), LocalTime.of(6, 0), LocalTime.of(7, 30), BigDecimal.valueOf(180000)),
                buildTimeSlot(field.getId(), LocalTime.of(7, 30), LocalTime.of(9, 0), BigDecimal.valueOf(180000)),
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

    private void ensureTeam(User captain, String name, String description, Enums.TeamLevel level) {
        boolean teamExists = teamRepository.findAll().stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(name));
        if (teamExists) {
            return;
        }

        Team team = new Team();
        team.setCaptainId(captain.getId());
        team.setName(name);
        team.setDescription(description);
        team.setLevel(level);
        team.setCreatedAt(LocalDateTime.now());
        teamRepository.save(team);
    }
}
