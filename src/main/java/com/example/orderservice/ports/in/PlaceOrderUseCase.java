package com.example.orderservice.ports.in;

import com.example.orderservice.domain.Order;

public interface PlaceOrderUseCase {
    Order placeOrder(Order order);
}