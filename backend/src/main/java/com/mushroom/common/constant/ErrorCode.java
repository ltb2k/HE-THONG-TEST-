package com.mushroom.common.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi hệ thống chuẩn hóa theo Ma trận lỗi (SRS Mục 8).
 */
@Getter
public enum ErrorCode {

    // --- Xác thực & Phân quyền ---
    AUTH_UNAUTHORIZED("ERR-AUTH-401-01", HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không hợp lệ hoặc phiên đã hết hạn."),
    AUTH_ACCOUNT_LOCKED("ERR-AUTH-423-01", HttpStatus.valueOf(423), "Tài khoản tạm khóa 15 phút do đăng nhập sai nhiều lần."),
    AUTH_FORBIDDEN("ERR-AUTH-403-01", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này."),
    AUTH_VALIDATION_FAILED("ERR-AUTH-422-01", HttpStatus.UNPROCESSABLE_ENTITY, "Vui lòng kiểm tra lại thông tin đã nhập."),
    AUTH_ACCOUNT_EXISTS("ERR-AUTH-409-01", HttpStatus.CONFLICT, "Tài khoản đã tồn tại."),

    // --- Dữ liệu & Validation chung ---
    COMMON_VALIDATION_FAILED("ERR-VAL-422-01", HttpStatus.UNPROCESSABLE_ENTITY, "Dữ liệu chưa hợp lệ."),

    // --- Phòng & Khay ---
    FARM_ROOM_NOT_FOUND("ERR-FARM-404-01", HttpStatus.NOT_FOUND, "Không tìm thấy phòng trồng."),
    FARM_ROOM_CONFLICT("ERR-FARM-409-01", HttpStatus.CONFLICT, "Phòng đang được sử dụng và không thể thực hiện thao tác này."),
    TRAY_NOT_FOUND("ERR-TRAY-404-01", HttpStatus.NOT_FOUND, "Không tìm thấy khay trồng."),
    TRAY_CONFLICT("ERR-TRAY-409-01", HttpStatus.CONFLICT, "Khay đang được liên kết hoặc không khả dụng."),

    // --- Thuê khay ---
    LEASE_PACKAGE_NOT_FOUND("ERR-PKG-404-01", HttpStatus.NOT_FOUND, "Gói thuê không còn khả dụng."),
    LEASE_RESOURCE_UNAVAILABLE("ERR-RENT-409-01", HttpStatus.CONFLICT, "Không đủ khay khả dụng để thực hiện yêu cầu."),

    // --- Người dùng ---
    USER_NOT_FOUND("ERR-USER-404-01", HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản."),
    USER_CONFLICT("ERR-USER-409-01", HttpStatus.CONFLICT, "Không thể cập nhật tài khoản ở trạng thái hiện tại."),

    // --- IoT & Cảm biến ---
    IOT_DEVICE_UNAUTHORIZED("ERR-IOT-401-01", HttpStatus.UNAUTHORIZED, "Thiết bị chưa được xác thực."),
    IOT_DEVICE_NOT_FOUND("ERR-IOT-404-01", HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị."),
    IOT_DEVICE_CONFLICT("ERR-IOT-409-01", HttpStatus.CONFLICT, "Thiết bị đã tồn tại hoặc đang ở trạng thái không thể cập nhật."),
    IOT_DEVICE_UNAVAILABLE("ERR-IOT-503-01", HttpStatus.SERVICE_UNAVAILABLE, "Thiết bị hiện không phản hồi."),

    // --- Camera ---
    CAMERA_STREAM_UNAVAILABLE("ERR-CAM-503-01", HttpStatus.SERVICE_UNAVAILABLE, "Luồng camera hiện không khả dụng."),

    // --- Thu hoạch ---
    HARVEST_REQUEST_CONFLICT("ERR-HARV-409-01", HttpStatus.CONFLICT, "Chưa thể tạo yêu cầu thu hoạch cho khay này."),

    // --- Thông báo ---
    NOTIFICATION_DELIVERY_FAILED("ERR-NOT-503-01", HttpStatus.SERVICE_UNAVAILABLE, "Chưa thể gửi thông báo; trạng thái đã được lưu nếu có xác nhận."),

    // --- Hệ thống ---
    SYSTEM_SERVICE_UNAVAILABLE("ERR-SYS-503-01", HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ tạm thời không khả dụng."),
    SYSTEM_INTERNAL_ERROR("ERR-SYS-500-01", HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi. Vui lòng thử lại sau.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
