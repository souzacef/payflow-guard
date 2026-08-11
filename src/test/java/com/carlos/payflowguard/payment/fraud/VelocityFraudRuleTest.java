package com.carlos.payflowguard.payment.fraud;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.payment.service.FraudCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VelocityFraudRuleTest {

    @Mock
    private PaymentRepository paymentRepository;

    private VelocityFraudRule rule;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        rule = new VelocityFraudRule(paymentRepository);

        merchant = new Merchant();
        merchant.setId(42L);
    }

    @Test
    void shouldPassWhenRecentPaymentCountIsBelowBoundary() {
        when(paymentRepository.countByMerchantIdAndCreatedAtAfter(eq(42L), any(Instant.class)))
                .thenReturn(2L);

        FraudCheckResult result = rule.evaluate(merchant, 1000L);

        assertTrue(result.isPassed());
    }

    @Test
    void shouldFailWithReasonWhenRecentPaymentCountReachesBoundary() {
        when(paymentRepository.countByMerchantIdAndCreatedAtAfter(eq(42L), any(Instant.class)))
                .thenReturn(3L);

        FraudCheckResult result = rule.evaluate(merchant, 1000L);

        assertFalse(result.isPassed());
        assertEquals("Too many recent payment attempts for this merchant", result.getReason());
    }
}
