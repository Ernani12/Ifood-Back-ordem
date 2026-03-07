package com.example.orderservice.ports.in;

import com.example.orderservice.domain.Order;

import java.util.UUID;

public interface UpdateOrderStatusUseCase {
    Order updateStatus(UUID orderId, Order.OrderStatus status);
}
