package com.example.shoestore.repository;

import com.example.shoestore.model.Shoe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoeRepository extends MongoRepository<Shoe, String> {
    List<Shoe> findByBrandIgnoreCase(String brand);
    List<Shoe> findByCategoryIgnoreCase(String category);
}
