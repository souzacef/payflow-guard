package com.carlos.payflowguard.payment.repository;

import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByMerchantUser(User user, Pageable pageable);

    Page<Payment> findByMerchantUserAndMerchantId(User user, Long merchantId, Pageable pageable);

    Page<Payment> findByMerchantUserAndStatus(User user, PaymentStatus status, Pageable pageable);

    Page<Payment> findByMerchantUserAndMerchantIdAndStatus(
            User user,
            Long merchantId,
            PaymentStatus status,
            Pageable pageable
    );

    Page<Payment> findByMerchantId(Long merchantId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByMerchantIdAndStatus(Long merchantId, PaymentStatus status, Pageable pageable);

    long countByMerchantIdAndCreatedAtAfter(Long merchantId, Instant createdAt);

    Optional<Payment> findByMerchantIdAndIdempotencyKey(Long merchantId, String idempotencyKey);

    Optional<Payment> findByMerchantUserAndMerchantIdAndIdempotencyKey(User user, Long merchantId, String idempotencyKey);

    List<Payment> findByStatus(PaymentStatus status);
}