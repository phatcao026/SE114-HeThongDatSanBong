package com.example.backend.service;

import com.example.backend.dto.request.AuthRequest;
import com.example.backend.dto.request.PasswordResetRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.impl.AuthServiceImpl;
import com.example.backend.utils.Enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    private static final String JWT_SECRET = "01234567890123456789012345678901";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthOtpService authOtpService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(authService, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 86400000L);
        ReflectionTestUtils.setField(authService, "registrationOtpRequired", true);
    }

    @Test
    public void sendRegisterOtp_UnusedEmail_SendsNormalizedOtp() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);

        authService.sendRegisterOtp(" User@Example.com ");

        verify(authOtpService).sendRegistrationOtp("user@example.com");
    }

    @Test
    public void sendRegisterOtp_DuplicateEmail_RejectsRequest() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> authService.sendRegisterOtp("user@example.com"));

        assertEquals(409, exception.getStatusCode());
        verify(authOtpService, never()).sendRegistrationOtp(any());
    }

    @Test
    public void register_MissingOtpWhenRequired_RejectsRequest() {
        AuthRequest request = registerRequest(null, null);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> authService.register(request));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Registration OTP is required", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void register_NonPlayerRole_RejectsRequestBeforeOtpVerification() {
        AuthRequest request = registerRequest("123456", "OWNER");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> authService.register(request));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Only player accounts can be registered here", exception.getMessage());
        verify(authOtpService, never()).verifyRegistrationOtp(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void register_ValidOtp_CreatesPlayerAndDeletesOtp() {
        AuthRequest request = registerRequest("123456", null);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        AuthResponse response = authService.register(request);

        assertNotNull(response.getAccessToken());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("user@example.com", savedUser.getEmail());
        assertEquals("hashed", savedUser.getPassword());
        assertEquals("Test User", savedUser.getFullName());
        assertEquals(UserRole.PLAYER, savedUser.getRole());
        verify(authOtpService).verifyRegistrationOtp("user@example.com", "123456");
        verify(authOtpService).deleteRegistrationOtp("user@example.com");
    }

    @Test
    public void login_LockedAccount_RejectsRequest() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret1");

        User user = new User();
        user.setId(7L);
        user.setEmail("user@example.com");
        user.setPassword("hashed");
        user.setRole(UserRole.PLAYER);
        user.setIsLocked(true);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret1", "hashed")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> authService.login(request));

        assertEquals(403, exception.getStatusCode());
        assertEquals("Account is locked", exception.getMessage());
    }

    @Test
    public void login_ValidCredentials_ReturnsToken() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret1");

        User user = new User();
        user.setId(7L);
        user.setEmail("user@example.com");
        user.setPassword("hashed");
        user.setRole(UserRole.PLAYER);
        user.setIsLocked(false);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret1", "hashed")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response.getAccessToken());
    }

    @Test
    public void resetPassword_ValidOtp_UpdatesPasswordAndDeletesOtp() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("User@Example.com");
        request.setOtp("123456");
        request.setNewPassword("newpass");

        User user = new User();
        user.setId(7L);
        user.setEmail("user@example.com");
        user.setPassword("oldhash");
        user.setRole(UserRole.PLAYER);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("newhash");

        authService.resetPassword(request);

        assertEquals("newhash", user.getPassword());
        verify(authOtpService).verifyPasswordResetOtp("user@example.com", "123456");
        verify(authOtpService).deletePasswordResetOtp("user@example.com");
        verify(userRepository).save(user);
    }

    private AuthRequest registerRequest(String otp, String role) {
        AuthRequest request = new AuthRequest();
        request.setFullName(" Test User ");
        request.setEmail(" User@Example.com ");
        request.setPassword("secret1");
        request.setOtp(otp);
        request.setRole(role);
        return request;
    }
}
