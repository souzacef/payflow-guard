package com.carlos.payflowguard.payment.event;

import com.carlos.payflowguard.payment.entity.PaymentStatus;

public record PaymentRefundCreatedEvent(
        PaymentEventSnapshot payment,
        Long refundId,
        Long refundAmountMinor,
        Long totalRefundedAmountMinor,
        PaymentStatus previousStatus,
        PaymentStatus newStatus,
        String actor,
        String reason
) {
}
