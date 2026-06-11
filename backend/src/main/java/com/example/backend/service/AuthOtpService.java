package com.example.backend.service;

import com.example.backend.exception.AppException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthOtpService {
    private static final Logger log = LoggerFactory.getLogger(AuthOtpService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final String registrationPrefix;
    private final String resetPrefix;
    private final Duration otpTtl;
    private final boolean mailEnabled;
    private final boolean logEnabled;
    private final String mailUsername;
    private final String senderName;

    public AuthOtpService(StringRedisTemplate redisTemplate,
                          JavaMailSender mailSender,
                          @Value("${app.auth.otp.registration-prefix}") String registrationPrefix,
                          @Value("${app.auth.otp.reset-prefix}") String resetPrefix,
                          @Value("${app.auth.otp.ttl-minutes}") long otpTtlMinutes,
                          @Value("${app.auth.otp.mail-enabled}") boolean mailEnabled,
                          @Value("${app.auth.otp.log-enabled}") boolean logEnabled,
                          @Value("${spring.mail.username:}") String mailUsername,
                          @Value("${app.auth.otp.sender-name}") String senderName) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.registrationPrefix = registrationPrefix;
        this.resetPrefix = resetPrefix;
        this.otpTtl = Duration.ofMinutes(otpTtlMinutes);
        this.mailEnabled = mailEnabled;
        this.logEnabled = logEnabled;
        this.mailUsername = mailUsername;
        this.senderName = senderName;
    }

    public void sendRegistrationOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String otp = generateOtp();
        store(registrationPrefix, normalizedEmail, otp);
        try {
            deliverOtp(normalizedEmail, otp, "Registration OTP", "Your registration OTP is: ");
        } catch (AppException ex) {
            redisTemplate.delete(key(registrationPrefix, normalizedEmail));
            throw ex;
        }
    }

    public void sendPasswordResetOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String otp = generateOtp();
        store(resetPrefix, normalizedEmail, otp);
        try {
            deliverOtp(normalizedEmail, otp, "Password reset OTP", "Your password reset OTP is: ");
        } catch (AppException ex) {
            redisTemplate.delete(key(resetPrefix, normalizedEmail));
            throw ex;
        }
    }

    public void verifyRegistrationOtp(String email, String otp) {
        verify(registrationPrefix, email, otp);
    }

    public void verifyPasswordResetOtp(String email, String otp) {
        verify(resetPrefix, email, otp);
    }

    public void deleteRegistrationOtp(String email) {
        redisTemplate.delete(key(registrationPrefix, normalizeEmail(email)));
    }

    public void deletePasswordResetOtp(String email) {
        redisTemplate.delete(key(resetPrefix, normalizeEmail(email)));
    }

    private void store(String prefix, String email, String otp) {
        try {
            redisTemplate.opsForValue().set(key(prefix, email), otp, otpTtl);
        } catch (RuntimeException ex) {
            throw new AppException(503, "OTP service is unavailable");
        }
    }

    private void verify(String prefix, String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        String cleanOtp = cleanOtp(otp);

        try {
            Optional<String> savedOtp = Optional.ofNullable(redisTemplate.opsForValue().get(key(prefix, normalizedEmail)));
            if (savedOtp.isEmpty() || !savedOtp.get().equals(cleanOtp)) {
                throw new AppException(400, "OTP is invalid or expired");
            }
        } catch (AppException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AppException(503, "OTP service is unavailable");
        }
    }

    private void deliverOtp(String email, String otp, String subject, String textPrefix) {
        if (logEnabled) {
            log.info("OTP for {} is {}", email, otp);
        }

        if (!mailEnabled) {
            return;
        }
        if (!StringUtils.hasText(mailUsername)) {
            throw new AppException(500, "Mail username is not configured");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailUsername, senderName);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(textPrefix + otp + ". This OTP expires in " + otpTtl.toMinutes() + " minutes.");
            mailSender.send(message);
        } catch (Exception ex) {
            throw new AppException(500, "Could not send OTP email");
        }
    }

    private String generateOtp() {
        return String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String key(String prefix, String email) {
        return prefix + email;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new AppException(400, "Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String cleanOtp(String otp) {
        if (!StringUtils.hasText(otp)) {
            throw new AppException(400, "OTP is required");
        }

        return otp.trim();
    }
}
