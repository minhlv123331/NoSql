package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private String shoeId;
    private String shoeName;
    private String brand;
    private String imageUrl;
    private String size;
    private String color;
    private double price;
    private int quantity;

    /**
     * Tính thành tiền của món hàng này (đơn giá * số lượng)
     */
    public double getSubtotal() {
        return price * quantity;
    }
}
