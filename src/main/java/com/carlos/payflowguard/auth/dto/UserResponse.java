package com.carlos.payflowguard.auth.dto;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(Long id, String email, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}