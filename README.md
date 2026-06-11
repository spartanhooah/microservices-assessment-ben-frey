# Multi-Tenant Order & Notification System
This repository contains two Spring Boot microservices, **orders** and **notifications**, that form an event-driven 
order management platform. The orders service exposes a REST API for managing customers, products, and orders, and
publishes Kafka events when orders are created or updated. The notifications service consumes those events and logs the 
notification details.

## System Overview
```
[ Client (with JWT that encodes tenantId) ]
         │
         ▼ HTTP (base: /v1/eshop)
┌──────────────────────────────────────────┐
│              Orders Service              │
│  - Routes request to tenant's Postgres   │
│  - Validates & persists entities         │
│  - Enriches OrderEvent with customer     │
│    data before publishing                │
└──────────────────┬───────────────────────┘
                   │
                   ▼ Publish OrderEvent (key = tenantId)
        ┌──────────────────────┐
        │  Kafka: orders.topic │
        └──────────┬───────────┘
                   │
                   ▼ Consume
┌──────────────────────────────────────────┐
│           Notifications Service          │
│  - Deserializes OrderEvent via Jackson   │
│  - Logs customer name, email, item count │
│    and order status                      │
└──────────────────────────────────────────┘
```

**Flow:** A client sends a request to the orders service, which validates the JWT to determine the active tenant, routes 
the database operation to that tenant's Postgres instance, persists the change, and synchronously publishes an 
`OrderEvent` to `orders.topic` before returning a response. The notifications service consumes the event asynchronously
and logs the notification.

### Multitenancy
The orders service is multi-tenant. Each HTTP request must include a `Bearer` JWT token with a `tenantId` claim. 
`JwtTenantFilter` extracts the claim and sets it on a `ThreadLocal` (`TenantContext`), which 
`TenantAwareRoutingDataSource` uses to select the appropriate Postgres connection for the duration of the request.

Two tenants are pre-configured:

| Tenant key | Database           | Port |
|------------|--------------------|------|
| `companyA` | `orders_company_a` | 5432 |
| `companyB` | `orders_company_b` | 5433 |

The HMAC signing key for JWT validation is hardcoded in `JwtTokenProvider` (see 
[Design Decisions](#design-decisions--tradeoffs)).

## Assumptions & Dependencies
### Runtime Dependencies
| Dependency                   | Version           | Role                                                                                  |
|------------------------------|-------------------|---------------------------------------------------------------------------------------|
| Java                         | 25                | Language runtime; Java `record` types used throughout for immutable model/DTO objects |
| Spring Boot                  | 4.0.6             | Framework for both services                                                           |
| Spring Kafka                 | (Boot-managed)    | Kafka producer (orders) and consumer (notifications)                                  |
| Apache Kafka                 | (Boot-managed)    | Message broker; runs in KRaft mode (no ZooKeeper)                                     |
| PostgreSQL                   | latest            | Transactional store for orders, customers, and products                               |
| Jackson (`jackson-databind`) | (Boot-managed)    | JSON serialization on both producer and consumer sides                                |
| jjwt                         | 0.13.0            | JWT parsing and validation in the orders service                                      |
| Springdoc OpenAPI            | 3.0.3             | Swagger UI for the orders service                                                     |
| Lombok                       | (freefair plugin) | Boilerplate reduction (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, etc.)        |
| Docker + Docker Compose      | any recent        | Local orchestration                                                                   |
| Gradle                       | 9.5.1             | Build tool for both services (wrapper included)                                       |

### Architecture Assumptions
- **Customers and products must be created before orders.** The orders service looks up both by ID and throws 
`IllegalArgumentException` if either is not found.
- **Producer-side enrichment.** The `OrderEvent` carries customer contact data (email, full name) fetched at publish 
time. This keeps the notifications service entirely isolated from any HTTP dependency on the orders service.
- **Kafka topic auto-creation by the producer.** `KafkaTopicConfig` in the orders service creates `orders.topic` (3 
partitions, replication factor 1) on startup. The consumer has `auto-create: false`. This is fine for a development
example, but in production a better approach would be a full Kafka infrastructure setup with topics created by a 
separate admin process.
- **Notifications are log-only.** The notifications service logs the event payload; it does not send emails, push
notifications, or call any external system.

## How to Run
### Full System (Docker Compose)
Ensure ports 8080, 8081, 5432, and 5433 are free, then from the repository root:
```shell
# Start all services (Kafka, two Postgres instances, orders, notifications)
docker compose up -d --build

# Tail notifications logs to observe consumed events
docker compose logs -f notifications

# Tear down and remove volumes (clears Kafka offsets and Postgres data)
docker compose down -v
```

The orders service is available at `http://localhost:8080/v1/eshop`.  
Swagger UI: `http://localhost:8080/v1/eshop/swagger-ui/index.html`

### Tests
The orders service has Spock unit tests covering `OrderService` business logic (create order, update status, status
guard rails) and the `Customer`, `Order`, and `Product` mappers. All tests mock the database and Kafka layers. No
running infrastructure is required.

```shell
# Run tests for a single service
cd orders && ./gradlew test
cd notifications && ./gradlew test
```

## API Endpoints
All orders routes are prefixed with `/v1/eshop`. Include an `Authorization: Bearer <jwt>` header to target a specific
tenant; requests without a valid token default to the `companyA` database.

### Customers
**`POST /v1/eshop/customers`** — Create a customer
```http
POST /v1/eshop/customers
Content-Type: application/json
Authorization: Bearer <jwt>

{
  "firstName": "Jane",
  "lastName": "Doe",
  "emailAddress": "jane.doe@example.com",
  "streetAddress": "123 Main St",
  "city": "Springfield",
  "state": "IL",
  "zip": "62701",
  "phoneNumber": "555-867-5309"
}
```

Response `201 Created`:
```json
{
  "id": "0ee9f31f-9b0d-4fd8-9ca7-d9ec785477db",
  "firstName": "Jane",
  "lastName": "Doe",
  "emailAddress": "jane.doe@example.com",
  "streetAddress": "123 Main St",
  "city": "Springfield",
  "state": "IL",
  "zip": "62701",
  "phoneNumber": "555-867-5309"
}
```

### Products
**`POST /v1/eshop/products`** — Create a product
```http
POST /v1/eshop/products
Content-Type: application/json
Authorization: Bearer <jwt>

{
  "name": "Wireless Keyboard",
  "price": 79.99
}
```

Response `201 Created`:
```json
{
  "id": "eddd3ddb-e742-4ae5-9e49-af9a49991f88",
  "name": "Wireless Keyboard",
  "price": 79.99
}
```

### Orders
**`POST /v1/eshop/orders`** — Create an order
The `customerId` and each `productId` must reference records that already exist in the tenant's database.

```http
POST /v1/eshop/orders
Content-Type: application/json
Authorization: Bearer <jwt>

{
  "customerId": "0ee9f31f-9b0d-4fd8-9ca7-d9ec785477db",
  "products": [
    { "productId": "eddd3ddb-e742-4ae5-9e49-af9a49991f88", "quantity": 1 },
    { "productId": "54e1fec2-6592-4ddc-a979-8c2f536e90b1", "quantity": 2 }
  ],
  "status": "NEW"
}
```

Response `201 Created`:
```json
{
  "id": "bbbedd00-6672-4e84-87fa-1639ba3932d0",
  "customerId": "0ee9f31f-9b0d-4fd8-9ca7-d9ec785477db",
  "products": [
    { "productId": "eddd3ddb-e742-4ae5-9e49-af9a49991f88", "quantity": 1 },
    { "productId": "54e1fec2-6592-4ddc-a979-8c2f536e90b1", "quantity": 2 }
  ],
  "status": "NEW"
}
```

On success, an `OrderEvent` is synchronously published to `orders.topic` before the response is returned.

**`PATCH /v1/eshop/orders/{orderId}/status`** — Update an order's status
Valid statuses: `NEW`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELED`
```http
PATCH /v1/eshop/orders/bbbedd00-6672-4e84-87fa-1639ba3932d0/status
Content-Type: application/json
Authorization: Bearer <jwt>

"PAID"
```

Response `200 Ok`: same shape as the create response, with the updated `status`.

Guard rails enforced by `OrderService`:
- No-op (returns current state, no event published) if the new status equals the current status. Writes a warning log.
- Rejected (returns current state, no event published) if the order is already `SHIPPED` or `DELIVERED` and a `CANCELED`
request is received. Writes an error log.

## Design Decisions & Tradeoffs
### 1. Producer-side event enrichment
The `OrderEvent` published to Kafka includes the customer's email and full name. This data is fetched from the database 
at publish time. This eliminates the need for the notifications service to make any HTTP call back to the orders service 
to look up contact data, keeping the two services fully decoupled at the cost of a slightly larger message payload.

### 2. Byte-array transport with `ByteArrayJsonMessageConverter`
The orders service serializes `OrderEvent` to JSON using Spring Kafka's `JsonSerializer` via a `KafkaTemplate<String, 
OrderEvent>`. The producer's `JsonSerializer` stamps a `__TypeId__` header on each message containing the 
fully-qualified producer class name (`net.frey.orders.model.OrderEvent`). If the notifications service used Spring's
`JsonDeserializer`, it would reject the message because that class is in an untrusted package and doesn't exist on the 
consumer's classpath.

Using `ByteArrayDeserializer` + `ByteArrayJsonMessageConverter` bypasses all type-header logic entirely: the consumer 
receives raw bytes and Jackson deserializes them into `net.frey.notifications.model.OrderEvent` purely by matching field
names. This lets each service own its own copy of the event model with no shared library required.

### 3. Tenant ID as Kafka message key
`OrderService.sendOrderEvent()` uses the current tenant ID as the Kafka message key. This ensures that all events for a
given tenant are sent to the same partition, preserving per-tenant ordering without requiring a separate partitioning 
strategy.

### 4. Synchronous Kafka send in the orders service
`kafkaTemplate.send(...).get()` blocks until broker acknowledgement before returning the HTTP response. This guarantees
that no order response is returned to the caller unless the event has been durably accepted by Kafka, at the cost of 
added latency on the write path. If the broker is unavailable, the request fails with a `RuntimeException`. For a demo
application, this is fine, but if this were to move to production, the send would need to be handled asynchronously,
especially in high-throughput environments.

### 5. Hardcoded JWT secret
The HMAC signing key in `JwtTokenProvider` and the `spring.security.oauth2.resourceserver.jwt` property are both 
hardcoded placeholder strings. I did not have the time to write an OAuth service, and this seemed like an acceptable
shortcut for this exercise.

## AI disclosure
I ran into space constraints due to my local environment. I used Gemini to help me get the Docker builds happening off 
of the space-constrained `/` partition. I also have not developed Kafka components outside a Confluent environment, so I
got a little help figuring out how to use the more basic deserialization - hence design decision #2. Finally, I did have
Claude spruce up this readme in terms of the layout and formatting. The content is mine, however.