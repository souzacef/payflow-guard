package com.carlos.payflowguard.payment.entity;

import com.carlos.payflowguard.merchant.entity.Merchant;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_merchant_idempotency", columnNames = {"merchant_id", "idempotency_key"})
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private Long amountMinor;

    @Column(nullable = false)
    private Long refundedAmountMinor = 0L;

    @Column(nullable = false, length = 3)
    private String currency;

    private String description;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String fraudReason;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public Long getAmountMinor() {
        return amountMinor;
    }

    public Long getRefundedAmountMinor() {
        return refundedAmountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public List<Refund> getRefunds() {
        return refunds;
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

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public void setAmountMinor(Long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public void setRefundedAmountMinor(Long refundedAmountMinor) {
        this.refundedAmountMinor = refundedAmountMinor;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }

    public void setRefunds(List<Refund> refunds) {
        this.refunds = refunds;
    }
}