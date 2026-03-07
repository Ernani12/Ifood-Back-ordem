package com.example.orderservice.application;

import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.in.GetOrderUseCase;
import com.example.orderservice.ports.out.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<Order> getorderById(UUID id) {
        return orderRepository.findById(id);
    }
}
