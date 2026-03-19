package com.carlos.payflowguard.merchant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateMerchantRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    public CreateMerchantRequest() {
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getEmail() {
        return email;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}