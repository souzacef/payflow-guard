package com.carlos.payflowguard.payment.event;

public record PaymentEventSnapshot(
        Long paymentId,
        Long merchantId,
        Long amountMinor,
        String currency,
        String fraudReason
) {
}
