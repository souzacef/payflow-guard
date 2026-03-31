package com.carlos.payflowguard.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RefundPaymentRequest {

    @NotNull(message = "Refund amount is required")
    @Min(value = 1, message = "Refund amount must be greater than zero")
    private Long amountMinor;

    private String reason;

    public Long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(Long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}