package com.mushroom.user.service;

import com.mushroom.auth.repository.RefreshTokenRepository;
import com.mushroom.common.constant.ErrorCode;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.common.exception.AppException;
import com.mushroom.user.dto.AdminResetPasswordRequest;
import com.mushroom.user.dto.CreateUserRequest;
import com.mushroom.user.dto.UpdateProfileRequest;
import com.mushroom.user.dto.UpdateUserRequest;
import com.mushroom.user.dto.UserResponse;
import com.mushroom.user.entity.Role;
import com.mushroom.user.entity.User;
import com.mushroom.user.entity.UserStatus;
import com.mushroom.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Role role, UserStatus status, String search, Pageable pageable) {
        Page<User> userPage = userRepository.findByFilters(role, status, search, pageable);
        return PageResponse.of(userPage.map(UserResponse::fromEntity));
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new AppException(ErrorCode.AUTH_ACCOUNT_EXISTS);
        }

        // Tạo tài khoản bởi Admin -> mặc định ACTIVE và email đã verified (FR-AUTH-ADM-001)
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .emailVerifiedAt(Instant.now())
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
            if (request.getStatus() == UserStatus.DISABLED) {
                // Thu hồi mọi phiên nếu bị vô hiệu hóa
                refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
            }
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    public void disableUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);

        // Thu hồi toàn bộ phiên đăng nhập
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
    }

    @Override
    public void adminResetPassword(UUID id, AdminResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedAttempts();
        userRepository.save(user);

        // Thu hồi mọi phiên cũ của người dùng này
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);

        return UserResponse.fromEntity(userRepository.save(user));
    }
}
