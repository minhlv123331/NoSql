package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String orderCode;

    @Indexed
    private String userId;

    private String customerName;

    private String customerPhone;

    private String shippingAddress;

    private String note;

    private String paymentMethod; // "COD" hoặc "BANK_TRANSFER"

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private double totalAmount;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Tổng số lượng sản phẩm (đôi giày) trong đơn hàng
     */
    public int getTotalQuantity() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Kiểm tra xem đơn hàng có thể hủy được hay không (chỉ hủy được khi còn PENDING)
     */
    public boolean canCancel() {
        return OrderStatus.PENDING.equals(this.status);
    }
}
