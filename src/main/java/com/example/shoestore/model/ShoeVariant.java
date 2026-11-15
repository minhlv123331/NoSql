package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoeVariant {
    private String size;    // Ví dụ: "38", "39", "40", "41", "42"
    private String color;   // Ví dụ: "Trắng", "Đen", "Đỏ", "Xanh"
    private int stock;      // Số lượng tồn kho
}
