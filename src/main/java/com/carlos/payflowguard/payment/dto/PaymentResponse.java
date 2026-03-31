package com.carlos.payflowguard.payment.dto;

import com.carlos.payflowguard.payment.entity.PaymentStatus;

import java.time.Instant;

public class PaymentResponse {

    private Long id;
    private Long merchantId;
    private String merchantBusinessName;
    private Long amountMinor;
    private Long refundedAmountMinor;
    private String currency;
    private String description;
    private PaymentStatus status;
    private String fraudReason;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentResponse(
            Long id,
            Long merchantId,
            String merchantBusinessName,
            Long amountMinor,
            Long refundedAmountMinor,
            String currency,
            String description,
            PaymentStatus status,
            String fraudReason,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.merchantBusinessName = merchantBusinessName;
        this.amountMinor = amountMinor;
        this.refundedAmountMinor = refundedAmountMinor;
        this.currency = currency;
        this.description = description;
        this.status = status;
        this.fraudReason = fraudReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getMerchantBusinessName() {
        return merchantBusinessName;
    }

    public Long getAmountMinor() {
        return amountMinor;
    }

    public Long getRefundedAmountMinor() {
        return refundedAmountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}