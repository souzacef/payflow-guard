package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.fraud.FraudRule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudCheckService {

    private final List<FraudRule> fraudRules;

    public FraudCheckService(List<FraudRule> fraudRules) {
        this.fraudRules = fraudRules;
    }

    public FraudCheckResult evaluate(Merchant merchant, Long amountMinor) {
        for (FraudRule fraudRule : fraudRules) {
            FraudCheckResult result = fraudRule.evaluate(merchant, amountMinor);

            if (!result.isPassed()) {
                return result;
            }
        }

        return FraudCheckResult.passed();
    }
}