package com.mushroom.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chuẩn hóa response cho tất cả API.
 * <p>
 * Thành công: {@code {statusCode: 200, message: "...", data: {...}}}
 * Lỗi:       {@code {statusCode: 422, message: "...", error: "ERR-VAL-422-01"}}
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int statusCode;
    private String message;
    private T data;
    private String error;

    // --- Factory methods ---

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message("Thành công")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .statusCode(201)
                .message("Tạo thành công")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int statusCode, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .error(errorCode)
                .message(message)
                .build();
    }
}
