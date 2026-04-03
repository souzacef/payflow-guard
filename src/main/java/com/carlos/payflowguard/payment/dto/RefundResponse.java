package com.carlos.payflowguard.payment.dto;

import java.time.Instant;

public class RefundResponse {

    private Long id;
    private Long paymentId;
    private Long amountMinor;
    private String reason;
    private Instant createdAt;

    public RefundResponse(
            Long id,
            Long paymentId,
            Long amountMinor,
            String reason,
            Instant createdAt
    ) {
        this.id = id;
        this.paymentId = paymentId;
        this.amountMinor = amountMinor;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getAmountMinor() {
        return amountMinor;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}