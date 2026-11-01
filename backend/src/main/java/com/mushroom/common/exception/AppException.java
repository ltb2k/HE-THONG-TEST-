package com.mushroom.common.exception;

import com.mushroom.common.constant.ErrorCode;
import lombok.Getter;

/**
 * Exception nghiệp vụ dùng chung toàn hệ thống.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.customMessage = errorCode.getDefaultMessage();
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
