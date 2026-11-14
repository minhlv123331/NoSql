package com.example.shoestore.repository;

import com.example.shoestore.model.Shoe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoeRepository extends MongoRepository<Shoe, String> {
}