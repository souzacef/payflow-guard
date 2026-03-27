package com.carlos.payflowguard.webhook.dto;

import java.time.Instant;

public class WebhookEventResponse {

    private Long id;
    private String eventType;
    private String entityName;
    private Long entityId;
    private String payload;
    private Instant createdAt;

    public WebhookEventResponse(
            Long id,
            String eventType,
            String entityName,
            Long entityId,
            String payload,
            Instant createdAt
    ) {
        this.id = id;
        this.eventType = eventType;
        this.entityName = entityName;
        this.entityId = entityId;
        this.payload = payload;
        this.createdAt = createdAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}