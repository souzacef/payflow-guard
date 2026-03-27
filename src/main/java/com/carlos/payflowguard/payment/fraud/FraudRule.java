package com.carlos.payflowguard.payment.fraud;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.payment.service.FraudCheckResult;

public interface FraudRule {

    FraudCheckResult evaluate(Merchant merchant, Long amountMinor);
}