package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.fraud.FraudRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FraudCheckServiceTest {

    private final Merchant merchant = new Merchant();
    private final Long amountMinor = 1000L;

    @Test
    void shouldPassWhenAllRulesPass() {
        FraudRule firstRule = mock(FraudRule.class);
        FraudRule secondRule = mock(FraudRule.class);
        when(firstRule.evaluate(merchant, amountMinor)).thenReturn(FraudCheckResult.passed());
        when(secondRule.evaluate(merchant, amountMinor)).thenReturn(FraudCheckResult.passed());

        FraudCheckResult result = new FraudCheckService(List.of(firstRule, secondRule))
                .evaluate(merchant, amountMinor);

        assertTrue(result.isPassed());
        verify(firstRule).evaluate(merchant, amountMinor);
        verify(secondRule).evaluate(merchant, amountMinor);
    }

    @Test
    void shouldReturnFirstFailureUnchangedAndStopImmediately() {
        FraudRule firstRule = mock(FraudRule.class);
        FraudRule laterRule = mock(FraudRule.class);
        FraudCheckResult firstFailure = FraudCheckResult.failed("First rule rejected payment");
        when(firstRule.evaluate(merchant, amountMinor)).thenReturn(firstFailure);

        FraudCheckResult result = new FraudCheckService(List.of(firstRule, laterRule))
                .evaluate(merchant, amountMinor);

        assertSame(firstFailure, result);
        assertEquals("First rule rejected payment", result.getReason());
        verify(firstRule).evaluate(merchant, amountMinor);
        verifyNoInteractions(laterRule);
    }

    @Test
    void shouldReturnLaterFailureAfterEarlierRulePasses() {
        FraudRule firstRule = mock(FraudRule.class);
        FraudRule secondRule = mock(FraudRule.class);
        FraudCheckResult laterFailure = FraudCheckResult.failed("Second rule rejected payment");
        when(firstRule.evaluate(merchant, amountMinor)).thenReturn(FraudCheckResult.passed());
        when(secondRule.evaluate(merchant, amountMinor)).thenReturn(laterFailure);

        FraudCheckResult result = new FraudCheckService(List.of(firstRule, secondRule))
                .evaluate(merchant, amountMinor);

        assertSame(laterFailure, result);
        assertEquals("Second rule rejected payment", result.getReason());
        verify(firstRule).evaluate(merchant, amountMinor);
        verify(secondRule).evaluate(merchant, amountMinor);
    }

    @Test
    void shouldPassWhenRuleListIsEmpty() {
        FraudCheckResult result = new FraudCheckService(List.of())
                .evaluate(merchant, amountMinor);

        assertTrue(result.isPassed());
    }
}
