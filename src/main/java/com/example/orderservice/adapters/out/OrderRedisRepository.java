package com.example.orderservice.adapters.out;

import com.example.orderservice.domain.Order;
import com.example.orderservice.ports.out.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Primary;
import com.example.orderservice.adapters.out.OrderPostgresRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class OrderRedisRepository implements OrderRepository {

    private static final String ORDER_KEY_PREFIX = "order:";
    private static final long CACHE_TTL_MINUTES = 3;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderPostgresRepository postgresRepository;

    public OrderRedisRepository(RedisTemplate<String, String> redisTemplate,
                                ObjectMapper objectMapper,
                                OrderPostgresRepository postgresRepository) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.postgresRepository = postgresRepository;
    }

    @Override
    public Order save(Order order) {
        // always persist in postgres first; redis is just a cache
        Order persisted = postgresRepository.save(order);
        System.out.println("SALVANDO NO REDIS 🔥 (cache aside)");
        try {
            String key = ORDER_KEY_PREFIX + persisted.getId();
            String json = objectMapper.writeValueAsString(persisted);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            return persisted;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar pedido no Redis", e);
        }
    }

    @Override
    public Optional<Order> findById(UUID id) {
        try {
            String key = ORDER_KEY_PREFIX + id;
            String json = redisTemplate.opsForValue().get(key);

            if (json != null) {
                Order order = objectMapper.readValue(json, Order.class);
                return Optional.of(order);
            }

            // cache miss -> load from postgres and populate cache
            Optional<Order> fromDb = postgresRepository.findById(id);
            fromDb.ifPresent(ord -> {
                try {
                    String j = objectMapper.writeValueAsString(ord);
                    redisTemplate.opsForValue().set(key, j, CACHE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                } catch (Exception ignored) {
                }
            });
            return fromDb;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao recuperar pedido do Redis", e);
        }
    }
}
