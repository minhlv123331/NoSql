package com.example.shoestore.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Chờ xác nhận", "bg-warning text-dark"),
    CONFIRMED("Đã xác nhận", "bg-info text-dark"),
    SHIPPING("Đang giao hàng", "bg-primary text-white"),
    DELIVERED("Đã giao thành công", "bg-success text-white"),
    CANCELLED("Đã hủy", "bg-danger text-white");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }
}
