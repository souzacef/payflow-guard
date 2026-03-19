package com.carlos.payflowguard.merchant.dto;

public class CreateMerchantRequest {

    private String businessName;
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