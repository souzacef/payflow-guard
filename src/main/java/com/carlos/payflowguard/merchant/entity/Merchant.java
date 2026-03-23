package com.carlos.payflowguard.merchant.entity;

import com.carlos.payflowguard.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;
    private String email;
    private String status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Merchant() {
    }

    public Merchant(Long id, String businessName, String email, String status, User user) {
        this.id = id;
        this.businessName = businessName;
        this.email = email;
        this.status = status;
        this.user = user;
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

    public User getUser() {
        return user;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUser(User user) {
        this.user = user;
    }
}