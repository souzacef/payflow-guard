# PayFlow Guard 💳🛡️

[🇧🇷 Versão em Português](README.pt-BR.md)

PayFlow Guard is a backend API for merchant and payment management with a focus on security, scalability, and fraud-aware architecture.

Built with Java 21 and Spring Boot, the project models a real-world payment processing backend, including authentication, merchant management, payment lifecycle, refunds, idempotency, audit logs, automatic capture, and webhook delivery with retry.

---

## 🎯 Purpose

This project was designed to simulate a real-world payment backend similar to systems used by fintech companies.

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
* Multi-tenant data isolation (users only access their own data)
* Role-based access control (USER / ADMIN)

### 🏪 Merchant Management

* Create, update, delete merchants
* Pagination, filtering, and sorting
* Merchant status management (ACTIVE / INACTIVE)
* User-scoped data access

### 💳 Payment System

* Create payments linked to merchants
* Full lifecycle:
  * `PENDING → AUTHORIZED → CAPTURED → REFUNDED / FAILED`
* Strict state transition validation
* Admin-controlled state updates
* Automatic capture via scheduler

### 🧪 Fraud Detection

* Automatic fraud validation on payment creation
* Fraud rules:
  * High-value transactions blocked
  * Rapid repeated transactions blocked
* Fraud reason stored and returned in API

### 🔁 Idempotency

* Prevents duplicate payment creation
* Uses `Idempotency-Key` header
* Same request returns the same payment
* Scoped per merchant

### 💸 Refund System

* Partial refunds supported
* Multiple refunds per payment
* Aggregated refund tracking
* Individual refund records persisted

### 📜 Refund History

* `GET /api/v1/payments/{id}/refunds`
* Full refund timeline per payment

### ⚙️ Automatic Capture

* Scheduled process:
  * `AUTHORIZED → CAPTURED`
* Emits audit log and webhook

### 📡 Webhooks

* Event: `payment.status.updated`
* Real HTTP delivery
* Delivery tracking
* Retry mechanism for failures

### 🧾 Audit Logging

Tracks:

* payment status changes
* refunds
* overrides
* automatic operations

### 📊 API Design

* RESTful endpoints (`/api/v1/...`)
* Clean request/response DTOs
* Global exception handling
* Consistent error response structure
* Proper HTTP status codes

### 🧪 Tests

* Integration tests:
  * Idempotency
  * Refund history
  * Payment lifecycle transitions

---

## 🔐 Roles and Access Model

New users are created with the `USER` role by default.

Some operations are restricted to `ADMIN`, such as:

* payment status updates
* payment status overrides
* refunds
* webhook event inspection and operational flows

In the current development setup, admin privileges can be granted directly in PostgreSQL:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'user@test.com';
```

This keeps the application flow simple while still allowing administrative scenarios to be tested locally.

---

## 🔒 Security Highlights

* Stateless authentication using JWT
* Passwords stored using BCrypt hashing
* Endpoint protection via Spring Security filters
* User-level data isolation enforced at query level

---

## 🧱 Tech Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* PostgreSQL (Docker)
* JWT (authentication)
* Maven
* Swagger / OpenAPI (springdoc)

---

## 🏗️ Architecture

The project follows a layered architecture:

```text
Controller → Service → Repository → Database
```

* **Controller**: Handles HTTP requests/responses
* **Service**: Business logic and validation
* **Repository**: Data access (JPA)
* **DTOs**: Clean API contracts (no entity leakage)

### Key Design Decisions

* Use of DTOs to decouple API from persistence layer
* Enum modeling for controlled domain values
* Centralized exception handling with `@RestControllerAdvice`
* Stateless authentication with JWT
* Per-user data isolation enforced at query level

### Conceptual Architecture Diagram

![PayFlow Guard Architecture](docs/architecture-diagram.png)

---

## 🧠 Architecture Walkthrough

The system follows a layered architecture with clear separation of concerns:

```text
Controller → Service → Repository → Database
```

### 🔄 Request Flow

1. A request hits the **Controller**
2. The controller validates input and forwards it to the **Service layer**
3. The service:
   * applies business rules
   * enforces state transitions
   * handles fraud checks
   * ensures idempotency
4. The service interacts with the **Repository layer**
5. The repository persists or retrieves data from the **Database**
6. The response is mapped to a DTO and returned to the client

### ⚙️ Example: Payment Creation Flow

```text
Client
  ↓
PaymentController
  ↓
PaymentService
  ↓
FraudCheckService
  ↓
Idempotency validation
  ↓
PaymentRepository
  ↓
Database
  ↓
PaymentResponse
```

### 🔁 Example: Payment Lifecycle Update

```text
Client
  ↓
PaymentController
  ↓
PaymentService
  ↓
Validate transition
  ↓
Save payment
  ↓
Audit log
  ↓
Webhook event
  ↓
Response
```

### 💸 Example: Refund Flow

```text
Client
  ↓
PaymentController
  ↓
PaymentService
  ↓
Validate refund rules
  ↓
Create refund record
  ↓
Update refunded total
  ↓
Save payment
  ↓
Audit log
  ↓
Webhook event
  ↓
Response
```

### 📡 Example: Automatic Capture Flow

```text
Scheduler
  ↓
PaymentAutoCaptureService
  ↓
Find AUTHORIZED payments
  ↓
Update to CAPTURED
  ↓
Audit log
  ↓
Webhook event
```

### 🧩 Key Design Principles

* **Separation of concerns**
  Each layer has a focused responsibility

* **State-driven design**
  Payment lifecycle is enforced through controlled transitions

* **Idempotency-first mindset**
  Prevents duplicate financial operations

* **Auditability**
  Every critical action is traceable

* **Resilience**
  Webhooks are retried on failure

---

## 🔑 Authentication Flow

1. User registers:

```text
POST /api/v1/auth/register
```

2. User logs in:

```text
POST /api/v1/auth/login
```

3. API returns JWT token

4. Token is used in requests:

```text
Authorization: Bearer <token>
```

---

## 📦 Example Endpoints

### Get current user

```text
GET /api/v1/auth/me
```

### Create merchant

```text
POST /api/v1/merchants
```

### Get merchants (with pagination)

```text
GET /api/v1/merchants?page=0&size=20&sort=id,asc
```

### Update merchant status

```text
PATCH /api/v1/merchants/{id}/status
```

### Create payment

```text
POST /api/v1/payments
Header: Idempotency-Key
```

### Update payment status

```text
PATCH /api/v1/payments/{id}/status
```

### Refund payment

```text
POST /api/v1/payments/{id}/refund
```

### Refund history

```text
GET /api/v1/payments/{id}/refunds
```

---

## 📥 Example Response

```json
{
  "id": 1,
  "businessName": "My Store",
  "email": "store@email.com",
  "status": "ACTIVE",
  "createdAt": "2026-03-24T10:30:00Z",
  "updatedAt": "2026-03-24T10:30:00Z"
}
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

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### 4. Access Swagger UI

http://localhost:8080/swagger-ui/index.html

---

## 🧪 Example Test Flow

1. Register user
2. Login and get JWT
3. Authorize in Swagger
4. Create merchant
5. Create payment
6. Move payment to `AUTHORIZED`
7. Wait for automatic capture
8. Perform partial refunds
9. Retrieve refund history

---

## 📡 Webhook Delivery Behavior

Webhook events are persisted and tracked.

The system supports:

* real HTTP delivery
* automatic retry for failed deliveries
* tracking of response status codes
* storage of failure details for observability

If a target URL is invalid or unreachable, the event is marked as failed instead of being silently lost.

---

## 📸 API Preview

![Swagger Overview](docs/swagger-overview.png)
![Swagger Endpoints](docs/swagger-endpoints.png)
![Swagger Request](docs/swagger-request.png)

---

## 📌 Roadmap

* [x] Payment lifecycle
* [x] Fraud detection rules
* [x] Role-based access (ADMIN / USER)
* [x] Webhooks with retry
* [x] Refund system with history
* [x] Idempotency
* [x] Automatic capture
* [x] Integration tests
* [ ] External payment gateway integration
* [ ] FX / currency conversion
* [ ] Advanced fraud rules

---

## 👨‍💻 Author

Carlos Eduardo Freire de Souza  
Backend Developer focused on Java, APIs and scalable backend systems

GitHub: https://github.com/souzacef  
LinkedIn: https://linkedin.com/in/carlosefsouza

---

## 💡 Notes

This project was built as a portfolio piece with focus on:

* real-world backend patterns
* clean architecture
* production-like behavior
* stateful business rules

---

## 🧠 Final Thought

Payments are not just transactions.

They are **state machines with consequences**.
