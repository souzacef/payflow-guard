package com.carlos.payflowguard.webhook.dto;

import com.carlos.payflowguard.webhook.entity.WebhookEventStatus;

import java.time.Instant;

public class WebhookEventResponse {

    private Long id;
    private String eventType;
    private String entityName;
    private Long entityId;
    private String payload;
    private WebhookEventStatus status;
    private int attemptCount;
    private Instant lastAttemptAt;
    private Instant createdAt;

    public WebhookEventResponse(
            Long id,
            String eventType,
            String entityName,
            Long entityId,
            String payload,
            WebhookEventStatus status,
            int attemptCount,
            Instant lastAttemptAt,
            Instant createdAt
    ) {
        this.id = id;
        this.eventType = eventType;
        this.entityName = entityName;
        this.entityId = entityId;
        this.payload = payload;
        this.status = status;
        this.attemptCount = attemptCount;
        this.lastAttemptAt = lastAttemptAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getEntityName() { return entityName; }
    public Long getEntityId() { return entityId; }
    public String getPayload() { return payload; }
    public WebhookEventStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getCreatedAt() { return createdAt; }
}