package com.example.shoestore.repository;

import com.example.shoestore.model.Order;
import com.example.shoestore.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    Optional<Order> findByIdAndUserId(String id, String userId);
    long countByStatus(OrderStatus status);
}
