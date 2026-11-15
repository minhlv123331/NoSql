package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "shoes")
public class Shoe {
    @Id
    private String id;
    private String name;
    private String brand;
    private double price;
    private String imageUrl;
    private String category;
    private String description;
    
    @Builder.Default
    private List<ShoeVariant> variants = new ArrayList<>();

    /**
     * Tính tổng số lượng tồn kho của tất cả các biến thể
     */
    public int getTotalStock() {
        if (variants == null) {
            return 0;
        }
        return variants.stream()
                .mapToInt(ShoeVariant::getStock)
                .sum();
    }

    /**
     * Lấy danh sách các size còn hàng (stock > 0), loại bỏ trùng lặp và sắp xếp tự nhiên
     */
    public List<String> getAvailableSizes() {
        if (variants == null) {
            return Collections.emptyList();
        }
        return variants.stream()
                .filter(v -> v.getStock() > 0 && v.getSize() != null)
                .map(ShoeVariant::getSize)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra xem sản phẩm có hết hàng toàn bộ hay không
     */
    public boolean isOutOfStock() {
        return getTotalStock() <= 0;
    }

    /**
     * Kiểm tra xem một size cụ thể còn hàng không
     */
    public boolean hasStockForSize(String size) {
        if (variants == null || size == null) {
            return false;
        }
        return variants.stream()
                .anyMatch(v -> size.equalsIgnoreCase(v.getSize()) && v.getStock() > 0);
    }
}
