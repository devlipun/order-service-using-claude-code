# order-service

A Spring Boot microservice that manages the complete order lifecycle —
from placement through fulfilment and cancellation.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Build | Maven |
| Database | PostgreSQL 18 (H2 for local dev) |
| Migrations | Liquibase |
| Mapping | MapStruct 1.6.3 |
| Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Observability | Actuator · Micrometer · Prometheus · Logstash JSON |

## Project Structure

```
src/main/java/com/skmcore/orderservice/
├── controller/       REST endpoints (no business logic)
├── service/          Business logic interfaces + implementations
├── repository/       Spring Data JPA repositories
├── model/            JPA entities + OrderStatus state machine
├── dto/              Request/response records
├── mapper/           MapStruct mappers
├── config/           Security, OpenAPI configuration
├── exception/        Custom exceptions + @RestControllerAdvice
└── event/            Spring ApplicationEvent types
```

## Getting Started

### Prerequisites

- JDK 25+
- Maven 3.9+
- Docker (for integration tests and containerised runs)

### Run locally (H2 in-memory)

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.
Swagger UI: **http://localhost:8080/swagger-ui.html**
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:orderdb`)
Default credentials: `dev` / `dev`

### Run with a local PostgreSQL (staging profile)

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=orderdb
export DB_USER=order_user
export DB_PASSWORD=secret

mvn spring-boot:run -Dspring-boot.run.profiles=staging
```

### Run tests

```bash
# Unit tests only (fast, no Docker required)
mvn test -Dtest="*Test"

# All tests including Testcontainers integration tests
mvn verify
```

> Integration tests spin up a real PostgreSQL container automatically via Testcontainers.

### Build a production JAR

```bash
mvn package -DskipTests
java -Dspring.profiles.active=prod \
     -jar target/order-service-*.jar
```

## Docker

### Build the image

```bash
docker build -t order-service:latest .
```

### Run the container

```bash
docker run --rm \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=orderdb \
  -e DB_USER=order_user \
  -e DB_PASSWORD=secret \
  order-service:latest
```

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/orders` | Place a new order |
| `GET` | `/api/v1/orders/{id}` | Get order by UUID |
| `GET` | `/api/v1/orders/number/{orderNumber}` | Get order by order number |
| `GET` | `/api/v1/orders?customerId=…` | List orders for a customer |
| `GET` | `/api/v1/orders?status=…` | List orders by status |
| `PATCH` | `/api/v1/orders/{id}/status` | Transition order status |
| `DELETE` | `/api/v1/orders/{id}` | Cancel an order |

### Order Status State Machine

```
PENDING ──► CONFIRMED ──► PROCESSING ──► SHIPPED ──► DELIVERED
   │              │              │
   └──────────────┴──────────────┴──► CANCELLED
```

## Observability

| Endpoint | Purpose |
|----------|---------|
| `GET /actuator/health` | Liveness / readiness |
| `GET /actuator/prometheus` | Prometheus scrape endpoint |
| `GET /actuator/loggers` | Dynamic log-level changes |

Structured JSON logging (via logstash-logback-encoder) is active on the
`staging` and `prod` profiles.

## Configuration Reference

| Env var | Required for | Description |
|---------|-------------|-------------|
| `DB_HOST` | staging, prod | PostgreSQL hostname |
| `DB_PORT` | staging, prod | PostgreSQL port (default `5432`) |
| `DB_NAME` | staging, prod | Database name |
| `DB_USER` | staging, prod | Database username |
| `DB_PASSWORD` | staging, prod | Database password |
| `SPRING_PROFILES_ACTIVE` | all | Active profile (`staging` or `prod`) |
