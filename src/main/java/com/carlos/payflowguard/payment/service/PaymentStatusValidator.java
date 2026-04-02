package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.payment.entity.PaymentStatus;

import java.util.Map;
import java.util.Set;

public class PaymentStatusValidator {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = Map.of(
            PaymentStatus.PENDING, Set.of(PaymentStatus.AUTHORIZED, PaymentStatus.FAILED),
            PaymentStatus.AUTHORIZED, Set.of(PaymentStatus.CAPTURED),
            PaymentStatus.CAPTURED, Set.of(PaymentStatus.REFUNDED),
            PaymentStatus.REFUNDED, Set.of(),
            PaymentStatus.FAILED, Set.of()
    );

    public static void validateTransition(PaymentStatus current, PaymentStatus next) {
        if (current == next) {
            return; // idempotent update allowed
        }

        Set<PaymentStatus> allowed = ALLOWED_TRANSITIONS.get(current);

        if (allowed == null || !allowed.contains(next)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + current + " → " + next
            );
        }
    }
}