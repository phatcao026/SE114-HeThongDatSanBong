package com.example.backend.controller;

import com.example.backend.dto.request.UserCreateRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.exception.AppException;
import com.example.backend.service.UserService;
import com.example.backend.utils.TokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.getUserById(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        ensureCurrentUserOrAdmin(id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserCreateRequest request) {
        ensureCurrentUserOrAdmin(id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    private void ensureCurrentUserOrAdmin(Long userId) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        if (!currentUserId.equals(userId) && !TokenUtils.hasRole("ADMIN")) {
            throw new AppException(403, "Access denied");
        }
    }
}
