package com.example.backend.service;

import com.example.backend.dto.request.UserCreateRequest;
import com.example.backend.dto.response.UserResponse;

public interface UserService {
    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserCreateRequest request);
}
