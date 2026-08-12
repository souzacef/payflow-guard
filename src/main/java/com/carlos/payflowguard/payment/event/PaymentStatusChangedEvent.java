package com.carlos.payflowguard.payment.event;

import com.carlos.payflowguard.payment.entity.PaymentStatus;

public record PaymentStatusChangedEvent(
        PaymentEventSnapshot payment,
        PaymentStatus previousStatus,
        PaymentStatus newStatus,
        String actor,
        String reason,
        PaymentTransitionSource source
) {
}
