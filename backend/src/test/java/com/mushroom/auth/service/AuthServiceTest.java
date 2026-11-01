package com.mushroom.auth.service;

import com.mushroom.auth.dto.AuthResponse;
import com.mushroom.auth.dto.ChangePasswordRequest;
import com.mushroom.auth.dto.LoginRequest;
import com.mushroom.auth.dto.RegisterRequest;
import com.mushroom.auth.dto.VerifyEmailRequest;
import com.mushroom.auth.entity.TokenType;
import com.mushroom.auth.entity.VerificationToken;
import com.mushroom.auth.repository.RefreshTokenRepository;
import com.mushroom.auth.repository.VerificationTokenRepository;
import com.mushroom.common.constant.ErrorCode;
import com.mushroom.common.exception.AppException;
import com.mushroom.notification.EmailService;
import com.mushroom.security.JwtProvider;
import com.mushroom.user.dto.UserResponse;
import com.mushroom.user.entity.Role;
import com.mushroom.user.entity.User;
import com.mushroom.user.entity.UserStatus;
import com.mushroom.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", 1800000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
        ReflectionTestUtils.setField(authService, "verificationTokenExpirationMs", 900000L);
    }

    @Test
    @DisplayName("UC-AUTH-001: Đăng ký khách hàng thành công -> trạng thái EMAIL_UNVERIFIED")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("vana@gmail.com")
                .password("Password123456@")
                .phone("0912345678")
                .build();

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("argon2id_hash");
        when(jwtProvider.generateRandomToken()).thenReturn("mock-verify-token");

        UUID generatedId = UUID.randomUUID();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(generatedId);
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(Role.CUSTOMER, response.getRole());
        assertEquals(UserStatus.EMAIL_UNVERIFIED, response.getStatus());
        verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("UC-AUTH-001: Đăng ký với email đã tồn tại -> ném lỗi ERR-AUTH-409-01")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("existing@gmail.com")
                .password("Password123456@")
                .build();

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(ErrorCode.AUTH_ACCOUNT_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("UC-AUTH-002: Đăng nhập thành công khi email đã xác minh và mật khẩu đúng")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("active@gmail.com")
                .password("Password123456@")
                .build();

        User user = User.builder()
                .fullName("Active User")
                .email("active@gmail.com")
                .passwordHash("argon2id_hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtProvider.generateAccessToken(any(), any(), any())).thenReturn("mock-access-token");
        when(jwtProvider.generateRandomToken()).thenReturn("mock-refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    @DisplayName("UC-AUTH-002: Đăng nhập thất bại khi email chưa xác minh -> ERR-AUTH-403-01")
    void testLoginUnverifiedEmail() {
        LoginRequest request = LoginRequest.builder()
                .email("unverified@gmail.com")
                .password("Password123456@")
                .build();

        User user = User.builder()
                .email("unverified@gmail.com")
                .passwordHash("argon2id_hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.EMAIL_UNVERIFIED)
                .build();

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));
        assertEquals(ErrorCode.AUTH_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("UC-AUTH-002: Khóa tài khoản sau 5 lần sai mật khẩu (BR-SEC-002) -> ERR-AUTH-423-01")
    void testBruteForceLockout() {
        LoginRequest request = LoginRequest.builder()
                .email("user@gmail.com")
                .password("WrongPassword123@")
                .build();

        User user = User.builder()
                .email("user@gmail.com")
                .passwordHash("argon2id_hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(4) // lần thứ 5 sẽ bị khóa
                .build();

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));
        assertEquals(ErrorCode.AUTH_ACCOUNT_LOCKED, ex.getErrorCode());
        assertTrue(user.isAccountLocked());
    }

    @Test
    @DisplayName("UC-AUTH-003: Xác minh email thành công -> chuyển sang ACTIVE")
    void testVerifyEmailSuccess() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .email("verify@gmail.com")
                .status(UserStatus.EMAIL_UNVERIFIED)
                .build();
        user.setId(userId);

        VerificationToken token = VerificationToken.builder()
                .userId(userId)
                .tokenHash("valid-token")
                .type(TokenType.EMAIL_VERIFY)
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        VerifyEmailRequest request = VerifyEmailRequest.builder()
                .email("verify@gmail.com")
                .token("valid-token")
                .build();

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByTokenHashAndTypeAndUsedAtIsNull("valid-token", TokenType.EMAIL_VERIFY))
                .thenReturn(Optional.of(token));

        authService.verifyEmail(request);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getEmailVerifiedAt());
        assertNotNull(token.getUsedAt());
    }

    @Test
    @DisplayName("UC-AUTH-004: Đổi mật khẩu thành công và thu hồi các phiên cũ (BR-SEC-002)")
    void testChangePasswordSuccess() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .passwordHash("old_argon2_hash")
                .build();
        user.setId(userId);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("CurrentPass123@")
                .newPassword("NewSecurePass123@")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getCurrentPassword(), "old_argon2_hash")).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new_argon2_hash");

        authService.changePassword(userId, request);

        assertEquals("new_argon2_hash", user.getPasswordHash());
        verify(refreshTokenRepository).revokeAllByUserId(any(), any());
    }
}
