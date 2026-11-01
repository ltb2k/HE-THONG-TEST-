package com.mushroom.rental.entity;

public enum LeaseStatus {
    PENDING,        // Khách hàng gửi yêu cầu, chờ duyệt
    APPROVED,       // Operator/Admin đã duyệt
    REJECTED,       // Operator/Admin từ chối
    ASSIGNED,       // Đã gán khay và cấp mã liên kết 24h
    ACTIVE,         // Khách hàng đã liên kết mã, khay đang nuôi trồng
    COMPLETED,      // Hết hạn hoặc hoàn thành chu kỳ thuê
    CANCELLED       // Bị hủy bởi Khách hàng (trước ASSIGNED) hoặc Operator/Admin
}
