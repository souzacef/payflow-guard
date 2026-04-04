# PayFlow Guard 💳🛡️

[🇧🇷 Versão em Português](README.pt-BR.md)

PayFlow Guard is a backend API for merchant and payment management with a focus on security, scalability, and fraud-aware architecture.

Built with Java 21 and Spring Boot, the project models a real-world payment processing backend, including authentication, merchant management, payment lifecycle, refunds, and webhook systems.

---

## 🎯 Purpose

This project simulates a real-world payment backend similar to systems used by fintech companies.

It focuses on:

* secure transaction flows
* multi-tenant data isolation
* scalable API design
* clean architecture principles
* state-driven payment processing

---

## 🚀 Features

### 🔐 Authentication & Security

* JWT-based authentication
* BCrypt password hashing
* Secure login and registration
* Protected endpoints with token validation
* Multi-tenant data isolation
* Role-based access control (USER / ADMIN)

---

### 🏪 Merchant Management

* Create, update, delete merchants
* Pagination, filtering, and sorting
* Merchant status (ACTIVE / INACTIVE)
* User-scoped data access

---

### 💳 Payment System

* Create payments linked to merchants
* Full lifecycle:

```
PENDING → AUTHORIZED → CAPTURED → REFUNDED / FAILED
```

* Strict transition validation
* Admin-controlled state updates
* Automatic capture (scheduler)

---

### 🧪 Fraud Detection

* Automatic validation during payment creation
* Rules implemented:

  * High-value transaction blocking
  * Velocity-based blocking (too many attempts)
* Fraud reason stored and returned in API

---

### 🔁 Idempotency (Production-grade)

* Prevents duplicate payments
* Uses `Idempotency-Key` header
* Same request = same response
* Scoped per merchant

---

### 💸 Refund System

* Partial refunds supported
* Multiple refunds per payment
* Tracks:

  * total refunded amount
  * individual refund records

---

### 📜 Refund History

```
GET /api/v1/payments/{id}/refunds
```

Returns full refund timeline per payment.

---

### ⚙️ Automatic Capture

* Scheduled process:

```
AUTHORIZED → CAPTURED
```

* Emits audit logs and webhooks

---

### 📡 Webhooks

* Event: `payment.status.updated`
* Stores delivery attempts
* Retry mechanism for failures

---

### 🧾 Audit Logging

Tracks:

* payment status changes
* refunds
* overrides
* automatic operations

---

### 📊 API Design

* RESTful endpoints (`/api/v1/...`)
* Clean DTO-based responses
* Global exception handling
* Consistent error format
* Proper HTTP status codes

---

### 🧪 Tests

* Integration tests:

  * Idempotency
  * Refund history

---

## 🔒 Security Highlights

* Stateless authentication (JWT)
* BCrypt password hashing
* Spring Security filters
* Per-user data isolation enforced at query level

---

## 🧱 Tech Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* PostgreSQL (Docker)
* JWT
* Maven
* Swagger / OpenAPI

---

## 🏗️ Architecture

```
Controller → Service → Repository → Database
```

* **Controller** → HTTP layer
* **Service** → business logic
* **Repository** → persistence
* **DTOs** → API contracts

### Cross-cutting concerns

* Security (JWT)
* Audit logging
* Webhooks
* Scheduler

---

## 🔑 Authentication Flow

1. Register:

```
POST /api/v1/auth/register
```

2. Login:

```
POST /api/v1/auth/login
```

3. Use token:

```
Authorization: Bearer <token>
```

---

## 📦 Example Endpoints

### Create Payment

```
POST /api/v1/payments
Headers:
  Idempotency-Key: abc-123
```

### Update Payment Status

```
PATCH /api/v1/payments/{id}/status
```

### Refund Payment

```
POST /api/v1/payments/{id}/refund
```

### Refund History

```
GET /api/v1/payments/{id}/refunds
```

---

## ⚙️ Running Locally

### 1. Clone repository

```bash
git clone https://github.com/souzacef/payflow-guard.git
cd payflow-guard
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Run application

```bash
./mvnw spring-boot:run
```

### 4. Swagger UI

http://localhost:8080/swagger-ui/index.html

---

## 🧪 Example Flow

1. Register user
2. Login and obtain JWT
3. Create merchant
4. Create payment
5. Authorize payment
6. Wait for automatic capture
7. Perform partial refunds
8. Retrieve refund history

---

## 📸 API Preview

![Swagger Overview](docs/swagger-overview.png)
![Swagger Endpoints](docs/swagger-endpoints.png)
![Swagger Request](docs/swagger-request.png)

---

## 📌 Roadmap

* [x] Payment lifecycle
* [x] Fraud detection
* [x] Role-based access
* [x] Webhooks with retry
* [x] Refund system with history
* [x] Idempotency
* [ ] External payment gateway integration
* [ ] FX / currency conversion
* [ ] Advanced fraud rules

---

## 👨‍💻 Author

Carlos Eduardo Freire de Souza
Backend Developer focused on Java, APIs, and AI

GitHub: https://github.com/souzacef
LinkedIn: https://linkedin.com/in/carlosefsouza

---

## 💡 Notes

This project was built as a portfolio piece with focus on:

* real-world backend architecture
* production-like behavior
* stateful business logic
* interview readiness

---

## 🧠 Final Thought

Payments are not just transactions.

They are **state machines with consequences**.
