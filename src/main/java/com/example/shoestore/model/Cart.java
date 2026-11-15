package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    /**
     * Tính tổng số tiền của toàn bộ sản phẩm trong giỏ hàng
     */
    public double getTotalAmount() {
        if (items == null) {
            return 0.0;
        }
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    /**
     * Tính tổng số lượng sản phẩm (chiếc/đôi) trong giỏ hàng
     */
    public int getTotalItems() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
