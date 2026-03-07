package com.example.orderservice.application;

import com.example.orderservice.domain.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisCartService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String KEY_PREFIX = "cart:";

    public RedisCartService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveCartTemporarily(String customerId, Order order) {
        try {
            String key = KEY_PREFIX + customerId;
            String json = objectMapper.writeValueAsString(order);
            
            // 3 minutos para teste local
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(3));
            System.out.println("🔥 Redis: Carrinho salvo com sucesso para " + customerId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar carrinho", e);
        }
    }

    public Order getCart(String customerId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + customerId);
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao ler carrinho do Redis", e);
        }
    }

    public void deleteCart(String customerId) {
        redisTemplate.delete(KEY_PREFIX + customerId);
    }

    public boolean cartExists(String customerId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + customerId));
    }
}