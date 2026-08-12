# 📊 PayFlow Guard — Feature Tracker

This document summarizes the implemented features and current project status.

---

## 🧱 Foundation

- [x] Spring Boot project setup (Java 21)
- [x] PostgreSQL integration (Docker)
- [x] Layered architecture (Controller → Service → Repository)
- [x] Global exception handling
- [x] DTO-based API design

---

## 🔐 Authentication & Security

- [x] JWT authentication (login/register)
- [x] Password hashing with BCrypt
- [x] Spring Security configuration
- [x] Role-based access control (ADMIN / USER)
- [x] Endpoint protection with token validation
- [x] JWT signing material supplied through `JWT_SECRET`

---

## 🏪 Merchant Management

- [x] Merchant CRUD
- [x] Pagination, filtering, sorting
- [x] Merchant status (ACTIVE / INACTIVE)
- [x] User-scoped data access (multi-tenant isolation)

---

## 💳 Payment System

- [x] Payment creation
- [x] Payment lifecycle:
  - [x] PENDING → AUTHORIZED
  - [x] AUTHORIZED → CAPTURED
  - [x] CAPTURED → REFUNDED after a full refund
  - [x] PENDING / AUTHORIZED → FAILED
- [x] Strict lifecycle transition validation
- [x] Admin-controlled status updates

---

## 🧪 Fraud Detection

- [x] High-value transaction blocking
- [x] Velocity-based blocking
- [x] Fraud reason tracking
- [x] Common `FraudRule` contract with Spring-discovered handlers
- [x] Explicit rule ordering and first-failure short-circuiting

---

## 🔁 Idempotency

- [x] Idempotent payment creation via `Idempotency-Key`
- [x] Duplicate request protection
- [x] Scoped per merchant

---

## 💸 Refund System

- [x] Partial refunds
- [x] Multiple refunds per payment
- [x] Aggregated refund tracking (`refundedAmountMinor`)
- [x] Individual refund records persisted

---

## 📜 Refund History

- [x] Endpoint:
  - `GET /api/v1/payments/{id}/refunds`
- [x] Full refund timeline per payment

---

## ⚙️ Automatic Capture

- [x] Scheduler for payment capture
- [x] Transition:
  - AUTHORIZED → CAPTURED
- [x] Publishes the same lifecycle event used by manual capture
- [x] Audit + webhook handled by observers

---

## 📡 Webhooks

- [x] Event: `payment.status.updated`
- [x] Persistent webhook event storage
- [x] Durable `PENDING` enqueue in the payment transaction
- [x] HTTP delivery only after commit
- [x] Retry mechanism for failed deliveries
- [x] HTTP response tracking (status + error)
- [x] `PAYFLOW_WEBHOOK_URL` override with loopback-only repository default

---

## 🧾 Audit Logging

- [x] Payment status changes tracked
- [x] Refund operations tracked
- [x] Automatic operations logged
- [x] Reason metadata stored
- [x] Mandatory audit observer participates before commit

---

## 🧪 Testing

- [x] Idempotency integration test
- [x] Refund history integration test
- [x] Payment lifecycle integration test
- [x] Fraud rule and chain coordinator unit tests
- [x] Observer and transaction-phase tests
- [x] H2 isolation with scheduling disabled
- [x] No external webhook traffic in the ordinary test suite

---

## 📊 Documentation

- [x] Swagger / OpenAPI integration
- [x] Bilingual README (EN / PT-BR)
- [x] Architecture walkthrough
- [x] Applied Design Patterns documentation
- [x] Feature tracker

---

## 📌 Roadmap (Future Work)

- [ ] External payment gateway integration
- [ ] FX / currency conversion
- [ ] Advanced fraud scoring system

---

## 🏁 Status

**Version:** v1.0.0  
**State:** Feature-complete for portfolio use  

This project models a realistic payment backend with production-inspired behavior, including lifecycle enforcement, idempotency, auditability, and failure handling.
