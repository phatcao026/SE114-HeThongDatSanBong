package com.example.backend.service.impl;

import com.example.backend.dto.request.UserCreateRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUser(id);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserCreateRequest request) {
        User user = findUser(id);

        if (request.getFullName() != null) {
            user.setFullName(cleanOptional(request.getFullName()));
        }

        if (request.getPhone() != null) {
            String phone = cleanOptional(request.getPhone());
            if (phone != null) {
                userRepository.findByPhone(phone)
                        .filter(existingUser -> !existingUser.getId().equals(id))
                        .ifPresent(existingUser -> {
                            throw new AppException(409, "Phone already exists");
                        });
            }
            user.setPhone(phone);
        }

        if (StringUtils.hasText(request.getPassword())) {
            validatePassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "User not found"));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setTrustScore(user.getTrustScore());
        
        // Calculate basic stats
        int matches = user.getBookings() != null ? (int) user.getBookings().stream()
                .filter(b -> b.getStatus() == com.example.backend.utils.Enums.BookingStatus.COMPLETED)
                .count() : 0;
        int noShows = user.getBookings() != null ? (int) user.getBookings().stream()
                .filter(b -> b.getStatus() == com.example.backend.utils.Enums.BookingStatus.CANCELLED)
                .count() : 0;
        
        response.setMatchesPlayed(matches);
        response.setNoShows(noShows);
        
        // Calculate average rating from trust score:
        // 100 -> 5.0
        // 80  -> 4.0
        // 60  -> 3.0
        // 20  -> 1.0
        double calculatedRating = (user.getTrustScore() != null ? user.getTrustScore() : 100) / 20.0;
        response.setAverageRating(Math.round(calculatedRating * 10.0) / 10.0);

        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private String cleanOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private void validatePassword(String password) {
        if (password.length() < 6) {
            throw new AppException(400, "Password must be at least 6 characters");
        }
    }
}
