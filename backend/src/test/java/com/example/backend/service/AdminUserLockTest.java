package com.example.backend.service;

import com.example.backend.dto.request.AuthRequest;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.AdminDashboardServiceImpl;
import com.example.backend.service.impl.AuthServiceImpl;
import com.example.backend.utils.Enums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminUserLockTest {

    private UserRepository userRepository;
    private AdminDashboardService adminDashboardService;

    private PasswordEncoder passwordEncoder;
    private AuthOtpService authOtpService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        adminDashboardService = new AdminDashboardServiceImpl(
                userRepository,
                mock(com.example.backend.repository.FieldRepository.class),
                mock(com.example.backend.repository.BookingRepository.class),
                mock(com.example.backend.repository.TimeSlotRepository.class),
                mock(com.example.backend.repository.PaymentRepository.class),
                mock(com.example.backend.repository.MatchPostRepository.class),
                mock(com.example.backend.repository.MatchRequestRepository.class),
                mock(com.example.backend.repository.ReviewRepository.class),
                mock(com.example.backend.repository.ConversationRepository.class),
                mock(com.example.backend.repository.MessageRepository.class)
        );

        passwordEncoder = mock(PasswordEncoder.class);
        authOtpService = mock(AuthOtpService.class);
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                authOtpService
        );
    }

    @Test
    void testLockUserSuccess() {
        User user = new User();
        user.setId(10L);
        user.setRole(Enums.UserRole.PLAYER);
        user.setIsLocked(false);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        adminDashboardService.lockUser(10L);

        assertTrue(user.getIsLocked());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testLockAdminThrowsException() {
        User user = new User();
        user.setId(10L);
        user.setRole(Enums.UserRole.ADMIN);
        user.setIsLocked(false);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> {
            adminDashboardService.lockUser(10L);
        });

        assertEquals(400, exception.getStatusCode());
        assertEquals("Không thể khóa tài khoản quản trị viên", exception.getMessage());
        assertFalse(user.getIsLocked());
    }

    @Test
    void testUnlockUserSuccess() {
        User user = new User();
        user.setId(10L);
        user.setRole(Enums.UserRole.PLAYER);
        user.setIsLocked(true);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        adminDashboardService.unlockUser(10L);

        assertFalse(user.getIsLocked());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testLoginLockedUserThrowsException() {
        User user = new User();
        user.setId(10L);
        user.setEmail("banned@example.com");
        user.setPassword("hashed");
        user.setIsLocked(true);

        when(userRepository.findByEmailIgnoreCase("banned@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);

        AuthRequest request = new AuthRequest();
        request.setEmail("banned@example.com");
        request.setPassword("password");

        AppException exception = assertThrows(AppException.class, () -> {
            authService.login(request);
        });

        assertEquals(403, exception.getStatusCode());
        assertEquals("Account is locked", exception.getMessage());
    }
}
