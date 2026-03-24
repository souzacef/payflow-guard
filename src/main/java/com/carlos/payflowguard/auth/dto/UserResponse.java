package com.carlos.payflowguard.auth.dto;

import com.carlos.payflowguard.user.entity.Role;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String email;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(Long id, String email, Role role, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}