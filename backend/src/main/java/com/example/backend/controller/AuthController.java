package com.example.backend.controller;

import com.example.backend.dto.request.AuthRequest;
import com.example.backend.dto.request.OtpRequest;
import com.example.backend.dto.request.OtpVerifyRequest;
import com.example.backend.dto.request.PasswordResetRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-register-otp")
    public ResponseEntity<AuthResponse> sendRegisterOtp(@Valid @RequestBody OtpRequest request) {
        authService.sendRegisterOtp(request.getEmail());
        return ResponseEntity.ok(new AuthResponse(null, "Registration OTP sent"));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody OtpRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new AuthResponse(null, "Password reset OTP sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyPasswordResetOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(new AuthResponse(null, "OTP verified"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new AuthResponse(null, "Password reset successfully"));
    }
}
