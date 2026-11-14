package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shoes")
public class Shoe {
    @Id
    private String id;
    private String name;
    private String brand;
    private double price;
    private String imageUrl;
    private List<String> sizes;
}