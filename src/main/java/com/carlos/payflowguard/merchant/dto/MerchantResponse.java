package com.carlos.payflowguard.merchant.dto;

import com.carlos.payflowguard.merchant.entity.MerchantStatus;

import java.time.Instant;

public class MerchantResponse {

    private Long id;
    private String businessName;
    private String email;
    private MerchantStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public MerchantResponse() {
    }

    public MerchantResponse(Long id, String businessName, String email, MerchantStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.businessName = businessName;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getEmail() {
        return email;
    }

    public MerchantStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(MerchantStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}