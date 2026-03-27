package com.carlos.payflowguard.payment.fraud;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.service.FraudCheckResult;
import org.springframework.stereotype.Component;

@Component
public class AmountThresholdFraudRule implements FraudRule {

    private static final long MAX_ALLOWED_AMOUNT_MINOR = 100000L;

    @Override
    public FraudCheckResult evaluate(Merchant merchant, Long amountMinor) {
        if (amountMinor > MAX_ALLOWED_AMOUNT_MINOR) {
            return FraudCheckResult.failed("Amount exceeds allowed threshold");
        }

        return FraudCheckResult.passed();
    }
}