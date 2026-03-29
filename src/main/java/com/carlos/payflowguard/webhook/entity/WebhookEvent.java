package com.carlos.payflowguard.webhook.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "webhook_events")
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;

    private String entityName;

    private Long entityId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    private String targetUrl;

    private Integer responseStatusCode;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Enumerated(EnumType.STRING)
    private WebhookEventStatus status;

    private int attemptCount;

    private Instant lastAttemptAt;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.status = WebhookEventStatus.PENDING;
        this.attemptCount = 0;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getPayload() {
        return payload;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public Integer getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getLastError() {
        return lastError;
    }

    public WebhookEventStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setResponseStatusCode(Integer responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public void setStatus(WebhookEventStatus status) {
        this.status = status;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }
}