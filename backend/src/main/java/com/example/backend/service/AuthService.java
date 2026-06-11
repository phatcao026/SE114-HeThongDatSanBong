package com.example.backend.service;

import com.example.backend.dto.request.AuthRequest;
import com.example.backend.dto.request.PasswordResetRequest;
import com.example.backend.dto.response.AuthResponse;

public interface AuthService {
    void sendRegisterOtp(String email);

    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);

    void forgotPassword(String email);

    void verifyPasswordResetOtp(String email, String otp);

    void resetPassword(PasswordResetRequest request);
}
