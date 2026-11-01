package com.mushroom.auth.service;

import com.mushroom.auth.dto.AuthResponse;
import com.mushroom.auth.dto.ChangePasswordRequest;
import com.mushroom.auth.dto.ForgotPasswordRequest;
import com.mushroom.auth.dto.LoginRequest;
import com.mushroom.auth.dto.RefreshTokenRequest;
import com.mushroom.auth.dto.RegisterRequest;
import com.mushroom.auth.dto.ResetPasswordRequest;
import com.mushroom.auth.dto.VerifyEmailRequest;
import com.mushroom.user.dto.UserResponse;

import java.util.UUID;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerification(String email);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(UUID userId);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);
}
