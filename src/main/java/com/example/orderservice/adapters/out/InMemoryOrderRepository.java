package com.example.orderservice.adapters.out;

import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.out.OrderRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {
    private Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
            System.out.println("SALVANDO EM MEMORIA ⚠");

        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}