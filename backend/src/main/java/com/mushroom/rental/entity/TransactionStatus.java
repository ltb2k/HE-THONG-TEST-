package com.mushroom.rental.entity;

public enum TransactionStatus {
    SIMULATED,              // Giao dịch mô phỏng khởi tạo
    SIMULATED_CONFIRMED,    // Giao dịch mô phỏng được xác nhận khi duyệt/gán
    SIMULATED_REJECTED      // Giao dịch mô phỏng bị từ chối
}
