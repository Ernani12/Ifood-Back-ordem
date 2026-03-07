package com.example.orderservice.ports.in;

import com.example.orderservice.domain.Order;

import java.util.Optional;
import java.util.UUID;

public interface GetOrderUseCase {
    Optional<Order> getorderById(UUID id);
}
