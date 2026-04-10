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
  - [x] CAPTURED → REFUNDED
  - [x] Failure handling (FAILED)
- [x] Strict lifecycle transition validation
- [x] Admin-controlled status updates

---

## 🧪 Fraud Detection

- [x] High-value transaction blocking
- [x] Velocity-based blocking
- [x] Fraud reason tracking

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
- [x] Audit + webhook triggered automatically

---

## 📡 Webhooks

- [x] Event: `payment.status.updated`
- [x] Persistent webhook event storage
- [x] Retry mechanism for failed deliveries
- [x] HTTP response tracking (status + error)

---

## 🧾 Audit Logging

- [x] Payment status changes tracked
- [x] Refund operations tracked
- [x] Automatic operations logged
- [x] Reason metadata stored

---

## 🧪 Testing

- [x] Idempotency integration test
- [x] Refund history integration test
- [x] Payment lifecycle integration test

---

## 📊 Documentation

- [x] Swagger / OpenAPI integration
- [x] Bilingual README (EN / PT-BR)
- [x] Architecture walkthrough
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
