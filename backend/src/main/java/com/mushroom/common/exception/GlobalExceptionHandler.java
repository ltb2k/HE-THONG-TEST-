package com.mushroom.common.exception;

import com.mushroom.common.constant.ErrorCode;
import com.mushroom.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Xử lý ngoại lệ toàn cục, chuẩn hóa format lỗi trả về theo Ma trận lỗi SRS.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode ec = ex.getErrorCode();
        log.warn("Nghiệp vụ trả lỗi: [{} - {}] {}", ec.getCode(), ec.getHttpStatus().value(), ex.getCustomMessage());
        ApiResponse<Void> response = ApiResponse.error(
                ec.getHttpStatus().value(),
                ec.getCode(),
                ex.getCustomMessage()
        );
        return ResponseEntity.status(ec.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        if (details.isBlank()) {
            details = ErrorCode.COMMON_VALIDATION_FAILED.getDefaultMessage();
        }

        log.warn("Dữ liệu đầu vào không hợp lệ: {}", details);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.COMMON_VALIDATION_FAILED.getHttpStatus().value(),
                ErrorCode.COMMON_VALIDATION_FAILED.getCode(),
                details
        );
        return ResponseEntity.status(ErrorCode.COMMON_VALIDATION_FAILED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Truy cập bị từ chối: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.AUTH_FORBIDDEN.getHttpStatus().value(),
                ErrorCode.AUTH_FORBIDDEN.getCode(),
                ErrorCode.AUTH_FORBIDDEN.getDefaultMessage()
        );
        return ResponseEntity.status(ErrorCode.AUTH_FORBIDDEN.getHttpStatus()).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Xác thực thất bại: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.AUTH_UNAUTHORIZED.getHttpStatus().value(),
                ErrorCode.AUTH_UNAUTHORIZED.getCode(),
                ErrorCode.AUTH_UNAUTHORIZED.getDefaultMessage()
        );
        return ResponseEntity.status(ErrorCode.AUTH_UNAUTHORIZED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Lỗi không dự kiến phía máy chủ: ", ex);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.SYSTEM_INTERNAL_ERROR.getHttpStatus().value(),
                ErrorCode.SYSTEM_INTERNAL_ERROR.getCode(),
                ErrorCode.SYSTEM_INTERNAL_ERROR.getDefaultMessage()
        );
        return ResponseEntity.status(ErrorCode.SYSTEM_INTERNAL_ERROR.getHttpStatus()).body(response);
    }
}
