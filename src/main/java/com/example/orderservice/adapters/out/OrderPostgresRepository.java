package com.example.orderservice.adapters.out;

import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.out.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderPostgresRepository implements OrderRepository {
    private final OrderJpaRepository jpaRepository;

    public OrderPostgresRepository(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
