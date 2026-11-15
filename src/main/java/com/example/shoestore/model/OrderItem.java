package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private String shoeId;
    private String shoeName;
    private String brand;
    private String imageUrl;
    private String size;
    private String color;
    private double price;
    private int quantity;

    /**
     * Thành tiền của món hàng trong đơn
     */
    public double getSubtotal() {
        return price * quantity;
    }
}
