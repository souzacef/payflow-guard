package com.carlos.payflowguard.webhook.repository;

import com.carlos.payflowguard.webhook.entity.WebhookEvent;
import com.carlos.payflowguard.webhook.entity.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByStatusAndAttemptCountLessThan(WebhookEventStatus status, int attemptCount);
}