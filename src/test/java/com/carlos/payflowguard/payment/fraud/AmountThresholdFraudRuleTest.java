package com.carlos.payflowguard.payment.fraud;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.service.FraudCheckResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmountThresholdFraudRuleTest {

    private final AmountThresholdFraudRule rule = new AmountThresholdFraudRule();
    private final Merchant merchant = new Merchant();

    @Test
    void shouldPassWhenAmountIsBelowThreshold() {
        FraudCheckResult result = rule.evaluate(merchant, 99999L);

        assertTrue(result.isPassed());
    }

    @Test
    void shouldPassWhenAmountEqualsThreshold() {
        FraudCheckResult result = rule.evaluate(merchant, 100000L);

        assertTrue(result.isPassed());
    }

    @Test
    void shouldFailWithReasonWhenAmountExceedsThreshold() {
        FraudCheckResult result = rule.evaluate(merchant, 100001L);

        assertFalse(result.isPassed());
        assertEquals("Amount exceeds allowed threshold", result.getReason());
    }
}
