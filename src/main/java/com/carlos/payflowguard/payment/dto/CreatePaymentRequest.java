package com.carlos.payflowguard.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePaymentRequest {

    @NotNull(message = "Merchant id is required")
    private Long merchantId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than zero")
    private Long amountMinor;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must have 3 characters")
    private String currency;

    private String description;

    public CreatePaymentRequest() {
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public Long getAmountMinor() {
        return amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public void setAmountMinor(Long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}