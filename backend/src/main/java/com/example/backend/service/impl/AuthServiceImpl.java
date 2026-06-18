package com.example.backend.service.impl;

import com.example.backend.dto.request.AuthRequest;
import com.example.backend.dto.request.PasswordResetRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthOtpService;
import com.example.backend.service.AuthService;
import com.example.backend.utils.Enums.UserRole;
import com.example.backend.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthOtpService authOtpService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.auth.registration-otp-required}")
    private boolean registrationOtpRequired;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthOtpService authOtpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authOtpService = authOtpService;
    }

    @Override
    public void sendRegisterOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new AppException(409, "Email already exists");
        }

        authOtpService.sendRegistrationOtp(normalizedEmail);
    }

    @Override
    @Transactional
    public AuthResponse register(AuthRequest request) {
        String email = normalizeEmail(request.getEmail());
        String fullName = cleanOptional(request.getFullName());
        String phone = cleanOptional(request.getPhone());

        if (!StringUtils.hasText(fullName)) {
            throw new AppException(400, "Full name is required");
        }

        validatePassword(request.getPassword());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException(409, "Email already exists");
        }

        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new AppException(409, "Phone already exists");
        }

        UserRole role = resolveRegisterRole(request.getRole());
        verifyRegistrationOtpIfNeeded(email, request.getOtp());

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        user.setTrustScore(100);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        authOtpService.deleteRegistrationOtp(email);

        String token = TokenUtils.generateToken(savedUser, jwtSecret, jwtExpirationMs);
        return new AuthResponse(token, "Register successfully");
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(401, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(401, "Invalid email or password");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException(403, "Account is locked");
        }

        String token = TokenUtils.generateToken(user, jwtSecret, jwtExpirationMs);
        return new AuthResponse(token, "Login successfully");
    }

    @Override
    public void forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new AppException(404, "User not found"));

        authOtpService.sendPasswordResetOtp(normalizedEmail);
    }

    @Override
    public void verifyPasswordResetOtp(String email, String otp) {
        authOtpService.verifyPasswordResetOtp(normalizeEmail(email), otp);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String email = normalizeEmail(request.getEmail());
        validatePassword(request.getNewPassword());
        authOtpService.verifyPasswordResetOtp(email, request.getOtp());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        authOtpService.deletePasswordResetOtp(email);
    }

    private void verifyRegistrationOtpIfNeeded(String email, String otp) {
        if (!StringUtils.hasText(otp)) {
            if (registrationOtpRequired) {
                throw new AppException(400, "Registration OTP is required");
            }
            return;
        }

        authOtpService.verifyRegistrationOtp(email, otp);
    }

    private UserRole resolveRegisterRole(String role) {
        if (!StringUtils.hasText(role)) {
            return UserRole.PLAYER;
        }

        try {
            UserRole userRole = UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
            if (userRole != UserRole.PLAYER) {
                throw new AppException(400, "Only player accounts can be registered here");
            }
            if (userRole == UserRole.OWNER) {
                if (userRepository.countByRole(UserRole.OWNER) > 0) {
                    throw new AppException(400, "Owner account already exists. Only one owner is allowed in the system.");
                }
            }
            return userRole;
        } catch (IllegalArgumentException ex) {
            throw new AppException(400, "Invalid role");
        }
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new AppException(400, "Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new AppException(400, "Password must be at least 6 characters");
        }
    }
}
