package com.mushroom.user.service;

import com.mushroom.common.dto.PageResponse;
import com.mushroom.user.dto.AdminResetPasswordRequest;
import com.mushroom.user.dto.CreateUserRequest;
import com.mushroom.user.dto.UpdateProfileRequest;
import com.mushroom.user.dto.UpdateUserRequest;
import com.mushroom.user.dto.UserResponse;
import com.mushroom.user.entity.Role;
import com.mushroom.user.entity.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    PageResponse<UserResponse> getUsers(Role role, UserStatus status, String search, Pageable pageable);

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void disableUser(UUID id);

    void adminResetPassword(UUID id, AdminResetPasswordRequest request);

    UserResponse getProfile(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
