package com.carlos.payflowguard.merchant.entity;

public class Merchant {

    private Long id;
    private String businessName;
    private String email;
    private String status;

    public Merchant() {
    }

    public Merchant(Long id, String businessName, String email, String status) {
        this.id = id;
        this.businessName = businessName;
        this.email = email;
        this.status = status;
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

    public String getStatus() {
        return status;
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

    public void setStatus(String status) {
        this.status = status;
    }
}