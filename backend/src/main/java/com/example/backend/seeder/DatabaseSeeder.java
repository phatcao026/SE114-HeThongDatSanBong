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

    @Value("${app.seed.admin.email:admin@se114.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:admin123456}")
    private String adminPassword;

    @Value("${app.seed.owner.email:owner@se114.local}")
    private String ownerEmail;

    @Value("${app.seed.owner.password:owner123456}")
    private String ownerPassword;

    @Value("${app.seed.player.email:player@se114.local}")
    private String playerEmail;

    @Value("${app.seed.player.password:player123456}")
    private String playerPassword;

    @Value("${app.seed.opponent.email:opponent@se114.local}")
    private String opponentEmail;

    @Value("${app.seed.opponent.password:opponent123456}")
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
        User admin = ensureUser(adminEmail, adminPassword, Enums.UserRole.ADMIN, "SE114 Admin");
        User owner = ensureUser(ownerEmail, ownerPassword, Enums.UserRole.OWNER, "SE114 Owner");
        User player = ensureUser(playerEmail, playerPassword, Enums.UserRole.PLAYER, "SE114 Player");
        User opponent = ensureUser(opponentEmail, opponentPassword, Enums.UserRole.PLAYER, "SE114 Opponent");

        Field field = ensureField(owner);
        ensureTimeSlots(field);
        ensureTeam(player);

        log.info("Seed data ready. admin={}, owner={}, player={}, opponent={}",
                admin.getEmail(), owner.getEmail(), player.getEmail(), opponent.getEmail());
    }

    private User ensureUser(String email, String password, Enums.UserRole role, String fullName) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email.trim().toLowerCase());
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRole(role);
                    user.setFullName(fullName);
                    user.setTrustScore(100);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    private Field ensureField(User owner) {
        List<Field> ownerFields = fieldRepository.findByOwnerId(owner.getId());
        if (!ownerFields.isEmpty()) {
            return ownerFields.getFirst();
        }

        Field field = new Field();
        field.setOwnerId(owner.getId());
        field.setName("SE114 Demo Field");
        field.setAddress("Thu Duc, Ho Chi Minh City");
        field.setDescription("Default seeded field for local development");
        field.setType(Enums.FieldType.FIVE_A_SIDE);
        field.setStatus(Enums.FieldStatus.AVAILABLE);
        field.setCoverImage(null);
        field.setCreatedAt(LocalDateTime.now());
        field.setUpdatedAt(LocalDateTime.now());
        return fieldRepository.save(field);
    }

    private void ensureTimeSlots(Field field) {
        if (!timeSlotRepository.findByFieldIdOrderByStartTimeAsc(field.getId()).isEmpty()) {
            return;
        }

        List<TimeSlot> timeSlots = List.of(
                buildTimeSlot(field.getId(), LocalTime.of(17, 0), LocalTime.of(18, 30), BigDecimal.valueOf(250000)),
                buildTimeSlot(field.getId(), LocalTime.of(18, 30), LocalTime.of(20, 0), BigDecimal.valueOf(300000)),
                buildTimeSlot(field.getId(), LocalTime.of(20, 0), LocalTime.of(21, 30), BigDecimal.valueOf(300000))
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

    private void ensureTeam(User captain) {
        if (!teamRepository.findByCaptainIdOrderByCreatedAtDesc(captain.getId()).isEmpty()) {
            return;
        }

        Team team = new Team();
        team.setCaptainId(captain.getId());
        team.setName("SE114 Demo Team");
        team.setDescription("Default seeded team for local development");
        team.setLevel(Enums.TeamLevel.BEGINNER);
        team.setCreatedAt(LocalDateTime.now());
        teamRepository.save(team);
    }
}
