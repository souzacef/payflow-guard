package com.carlos.payflowguard.payment.dto;

import com.carlos.payflowguard.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OverridePaymentStatusRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    @NotBlank(message = "Reason is required")
    private String reason;

    public OverridePaymentStatusRequest() {
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}