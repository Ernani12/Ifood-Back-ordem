# Order Service

Microservice responsible for managing orders. Implements hexagonal architecture.

## Key points
- Spring Boot application
- Kafka producer for order events (topic `orders`)
- PostgreSQL as primary database (entity `Order` mapped with JPA)
- Redis cache deployed in front of PostgreSQL (cache-aside pattern with 10‑minute TTL)
- REST controller under `/orders`

## Build & run
Projeto usa Docker para todos os componentes (Postgres, Redis, Kafka, order‑service).

1. Start containers:
   ```bash
   docker-compose up -d --build
   ```
2. Logs:
   ```bash
   docker logs -f order-service
   ```

O serviço ficará exposto em `http://localhost:8081/orders`.

> Internally the application talks to `postgres:5432` and `redis:6379` (service names).

Durante desenvolvimento sem Docker, use as variáveis de ambiente padrão em `application.properties`.
