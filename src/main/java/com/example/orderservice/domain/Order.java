package com.example.orderservice.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id") // cria FK na tabela order_items
    private List<OrderItem> items;

    private double total;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Construtores
    public Order() {}

    public Order(UUID id, String customerId, List<OrderItem> items, double total, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.total = total;
        this.status = status;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", items=" + items +
                ", total=" + total +
                ", status=" + status +
                '}';
    }

    public enum OrderStatus {
        CREATED, CONFIRMED, DELIVERED, CANCELLED
    }

    // Classe OrderItem como entidade JPA
    @Entity
    @Table(name = "order_items")
    public static class OrderItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue
        @Column(columnDefinition = "uuid")
        private UUID id;

        private String name;
        private int quantity;
        private double price;

        // Construtores
        public OrderItem() {}

        public OrderItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters e Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", quantity=" + quantity +
                    ", price=" + price +
                    '}';
        }
    }
}