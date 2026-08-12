package com.carlos.payflowguard.webhook.listener;

import com.carlos.payflowguard.webhook.event.WebhookDeliveryRequestedEvent;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WebhookDeliveryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebhookDeliveryEventListener.class);

    private final WebhookEventService webhookEventService;

    public WebhookDeliveryEventListener(WebhookEventService webhookEventService) {
        this.webhookEventService = webhookEventService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWebhookDeliveryRequested(WebhookDeliveryRequestedEvent event) {
        try {
            webhookEventService.deliverEvent(event.webhookEventId());
        } catch (RuntimeException ex) {
            logger.error("Webhook delivery failed after commit for event {}", event.webhookEventId(), ex);
        }
    }
}
