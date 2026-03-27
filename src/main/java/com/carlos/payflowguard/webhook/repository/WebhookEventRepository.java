package com.carlos.payflowguard.webhook.repository;

import com.carlos.payflowguard.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
}