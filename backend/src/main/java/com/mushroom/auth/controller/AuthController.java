package com.mushroom.auth.controller;

import com.mushroom.auth.dto.AuthResponse;
import com.mushroom.auth.dto.ChangePasswordRequest;
import com.mushroom.auth.dto.ForgotPasswordRequest;
import com.mushroom.auth.dto.LoginRequest;
import com.mushroom.auth.dto.RefreshTokenRequest;
import com.mushroom.auth.dto.RegisterRequest;
import com.mushroom.auth.dto.ResetPasswordRequest;
import com.mushroom.auth.dto.VerifyEmailRequest;
import com.mushroom.auth.service.AuthService;
import com.mushroom.common.dto.ApiResponse;
import com.mushroom.security.UserPrincipal;
import com.mushroom.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Xác minh địa chỉ email thành công. Bạn có thể đăng nhập ngay.", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        authService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi lại email xác minh nếu tài khoản hợp lệ.", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            authService.logout(userPrincipal.getId());
        }
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại trong hệ thống, hướng dẫn khôi phục mật khẩu đã được gửi.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.", null));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", null));
    }
}
