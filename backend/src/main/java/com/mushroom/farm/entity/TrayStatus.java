package com.mushroom.farm.entity;

public enum TrayStatus {
    AVAILABLE,      // Khay sẵn sàng cho thuê/phân công
    ASSIGNED,       // Khay đã được phân công, chờ khách hàng liên kết mã
    IN_USE,         // Khay đang được nuôi trồng tích cực (lease ACTIVE)
    MAINTENANCE,    // Khay đang vệ sinh/bảo dưỡng
    ARCHIVED        // Khay đã lưu trữ/ngừng hoạt động (BR-FARM-001)
}
