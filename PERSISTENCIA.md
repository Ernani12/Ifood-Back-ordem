# Status: Armazenamento de Pedidos

## Situação Atual ⚠️

O microserviço **Order Service** está rodando, mas **ESTÁ ARMAZENANDO EM MEMÓRIA**.

- **Imagem**: Docker ✅
- **Kafka**: Integrado ✅
- **Persistência**: ❌ Em memória (InMemoryOrderRepository)

### Problema:
Se o container reiniciar, **todos os pedidos são perdidos**!

## Solução Proposta 

### Opção 1: Com Banco H2 (Mais Simples - Recomendado para DEV)
- Banco embedado, sem configuração
- Arquivo único `order-service.db`
- Persiste dados entre restarts

### Opção 2: Com PostgreSQL (Produção)
- Banco robusto
- Múltiplas instâncias
- Backup e replicação

> A arquitetura agora usa PostgreSQL como banco principal e Redis como cache (Cache‑Aside Pattern), com TTL de 10 minutos para leituras frequentes.

## Próximas Ações

1. **Adicionar dependências** JPA e PostgreSQL ao `pom.xml` (já feito)
2. **Anotar `Order` como @Entity** e criar `OrderJpaRepository` (implements JpaRepository)
3. **Implementar OrderPostgresRepository** e estender `OrderRedisRepository` para cache
4. **Atualizar `application.properties`** para apontar p/ Postgres
5. **Modificar `docker-compose.yml`** para incluir serviço `postgres` e variáveis de ambiente do Order Service
6. **Reconstruir** com `docker-compose up --build` e verificar criação da tabela `orders`

### Fluxo Funcionando Agora:

```
Angular (http://localhost:4200/pedido)
    ↓ [POST] Enviar Pedido
Docker Order Service (http://localhost:8081/orders)
    ↓ Salva (em memória por enquanto)
    ↓ Publica no Kafka topic: "orders-created"
Microserviço Entrega (consome Kafka)
```

## Comandos Docker

```bash
# Ver containers
docker ps

# Ver logs
docker logs order-service -f

# Parar
docker-compose down

# Iniciar
docker-compose up -d

# Rebuild
docker-compose up -d --build
```

## Dados Salvos Agora

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUSTOMER_123",
  "items": [
    {
      "name": "Pizza Margherita",
      "price": 45.50,
      "quantity": 2
    }
  ],
  "total": 91.00,
  "status": "CREATED"
}
```

> ⚠️ Esse pedido é perdido se o container reiniciar!
