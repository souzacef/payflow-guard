package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class FraudCheckService {

    private final PaymentRepository paymentRepository;

    public FraudCheckService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public FraudCheckResult evaluate(Merchant merchant, Long amountMinor) {
        if (amountMinor > 100000L) {
            return FraudCheckResult.failed("Amount exceeds allowed threshold");
        }

        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
        long recentPayments = paymentRepository.countByMerchantIdAndCreatedAtAfter(
                merchant.getId(),
                fiveMinutesAgo
        );

        if (recentPayments >= 3) {
            return FraudCheckResult.failed("Too many recent payment attempts for this merchant");
        }

        return FraudCheckResult.passed();
    }
}