package com.mushroom.auth.service;

import com.mushroom.auth.dto.AuthResponse;
import com.mushroom.auth.dto.ChangePasswordRequest;
import com.mushroom.auth.dto.ForgotPasswordRequest;
import com.mushroom.auth.dto.LoginRequest;
import com.mushroom.auth.dto.RefreshTokenRequest;
import com.mushroom.auth.dto.RegisterRequest;
import com.mushroom.auth.dto.ResetPasswordRequest;
import com.mushroom.auth.dto.VerifyEmailRequest;
import com.mushroom.auth.entity.RefreshToken;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;

    @Value("${jwt.access-token-expiration-ms:1800000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.verification-token-expiration-ms:900000}")
    private long verificationTokenExpirationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new AppException(ErrorCode.AUTH_ACCOUNT_EXISTS);
        }

        // Tạo tài khoản Customer ở trạng thái EMAIL_UNVERIFIED (BR-SEC-001)
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .status(UserStatus.EMAIL_UNVERIFIED)
                .build();

        user = userRepository.save(user);

        // Tạo token xác minh 15 phút (BR-SEC-001)
        String rawToken = jwtProvider.generateRandomToken();
        createVerificationToken(user.getId(), rawToken, TokenType.EMAIL_VERIFY, verificationTokenExpirationMs);

        // Gửi email xác minh
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), rawToken);

        return UserResponse.fromEntity(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));

        // Kiểm tra khóa tài khoản do brute-force (BR-SEC-002: sai 5 lần khóa 15 phút)
        if (user.isAccountLocked()) {
            throw new AppException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        // Kiểm tra mật khẩu Argon2id
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            if (user.isAccountLocked()) {
                throw new AppException(ErrorCode.AUTH_ACCOUNT_LOCKED);
            }
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        // Kiểm tra trạng thái tài khoản
        if (user.getStatus() == UserStatus.EMAIL_UNVERIFIED) {
            throw new AppException(ErrorCode.AUTH_FORBIDDEN, "Tài khoản chưa xác minh email. Vui lòng kiểm tra email để kích hoạt.");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new AppException(ErrorCode.AUTH_FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu hóa.");
        }

        // Reset số lần sai mật khẩu
        user.resetFailedAttempts();
        userRepository.save(user);

        // Tạo access token (30 phút) và refresh token (7 ngày) (BR-SEC-002)
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String rawRefreshToken = jwtProvider.generateRandomToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(rawRefreshToken)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));

        VerificationToken token = verificationTokenRepository
                .findByTokenHashAndTypeAndUsedAtIsNull(request.getToken().trim(), TokenType.EMAIL_VERIFY)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Mã xác minh không hợp lệ hoặc đã được sử dụng."));

        if (!token.getUserId().equals(user.getId()) || token.isExpired()) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Mã xác minh đã hết hạn hoặc không khớp.");
        }

        // Tiêu thụ token một lần
        token.setUsedAt(Instant.now());
        verificationTokenRepository.save(token);

        // Kích hoạt tài khoản
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    public void resendVerification(String email) {
        userRepository.findByEmailIgnoreCase(email.toLowerCase().trim())
                .ifPresent(user -> {
                    if (user.getStatus() == UserStatus.EMAIL_UNVERIFIED) {
                        verificationTokenRepository.invalidateExistingTokens(user.getId(), TokenType.EMAIL_VERIFY, Instant.now());
                        String rawToken = jwtProvider.generateRandomToken();
                        createVerificationToken(user.getId(), rawToken, TokenType.EMAIL_VERIFY, verificationTokenExpirationMs);
                        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), rawToken);
                    }
                });
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(request.getRefreshToken().trim())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Phiên đăng nhập đã hết hạn hoặc bị thu hồi.");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.AUTH_FORBIDDEN);
        }

        // Cấp access token mới
        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(storedToken.getTokenHash())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    public void logout(UUID userId) {
        // Thu hồi toàn bộ refresh session của user (BR-SEC-002)
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Luôn trả kết quả chung để chống dò email người dùng (SRS UC-AUTH-004 2)
        userRepository.findByEmailIgnoreCase(request.getEmail().toLowerCase().trim())
                .ifPresent(user -> {
                    if (user.getStatus() == UserStatus.ACTIVE) {
                        verificationTokenRepository.invalidateExistingTokens(user.getId(), TokenType.PASSWORD_RESET, Instant.now());
                        String rawToken = jwtProvider.generateRandomToken();
                        createVerificationToken(user.getId(), rawToken, TokenType.PASSWORD_RESET, verificationTokenExpirationMs);
                        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
                    }
                });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken token = verificationTokenRepository
                .findByTokenHashAndTypeAndUsedAtIsNull(request.getToken().trim(), TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Mã đặt lại mật khẩu không hợp lệ hoặc đã sử dụng."));

        if (token.isExpired()) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Mã đặt lại mật khẩu đã hết hạn.");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tiêu thụ token
        token.setUsedAt(Instant.now());
        verificationTokenRepository.save(token);

        // Lưu mật khẩu Argon2id mới và thu hồi mọi session cũ (BR-SEC-002)
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedAttempts();
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED, "Mật khẩu hiện tại không chính xác.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Thu hồi các refresh session khác (BR-SEC-002)
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
    }

    private void createVerificationToken(UUID userId, String rawToken, TokenType type, long durationMs) {
        VerificationToken token = VerificationToken.builder()
                .userId(userId)
                .tokenHash(rawToken)
                .type(type)
                .expiresAt(Instant.now().plusMillis(durationMs))
                .build();
        verificationTokenRepository.save(token);
    }
}
