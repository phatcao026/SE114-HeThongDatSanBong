package com.example.backend.service.impl;

import com.example.backend.dto.request.AuthRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.UserRepository;
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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(resolveRole(request.getRole()));
        user.setTrustScore(100);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
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

        String token = TokenUtils.generateToken(user, jwtSecret, jwtExpirationMs);
        return new AuthResponse(token, "Login successfully");
    }

    private UserRole resolveRole(String role) {
        if (!StringUtils.hasText(role)) {
            return UserRole.PLAYER;
        }

        try {
            UserRole userRole = UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
            if (userRole == UserRole.ADMIN) {
                throw new AppException(400, "Admin account cannot be registered here");
            }
            return userRole;
        } catch (IllegalArgumentException ex) {
            throw new AppException(400, "Invalid role");
        }
    }

    private String normalizeEmail(String email) {
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
