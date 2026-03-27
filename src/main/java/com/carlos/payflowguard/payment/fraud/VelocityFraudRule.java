package com.carlos.payflowguard.payment.fraud;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.payment.service.FraudCheckResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class VelocityFraudRule implements FraudRule {

    private final PaymentRepository paymentRepository;

    public VelocityFraudRule(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public FraudCheckResult evaluate(Merchant merchant, Long amountMinor) {
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