package com.carlos.payflowguard.payment.dto;

import com.carlos.payflowguard.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePaymentStatusRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    public UpdatePaymentStatusRequest() {
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}