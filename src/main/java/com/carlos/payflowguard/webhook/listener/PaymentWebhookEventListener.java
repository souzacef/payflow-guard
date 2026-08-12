package com.carlos.payflowguard.webhook.listener;

import com.carlos.payflowguard.payment.event.PaymentRefundCreatedEvent;
import com.carlos.payflowguard.payment.event.PaymentStatusChangedEvent;
import com.carlos.payflowguard.webhook.event.WebhookDeliveryRequestedEvent;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentWebhookEventListener {

    private final WebhookEventService webhookEventService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentWebhookEventListener(
            WebhookEventService webhookEventService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.webhookEventService = webhookEventService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
        Long webhookEventId = webhookEventService.enqueuePaymentStatusUpdated(
                event.payment(),
                event.previousStatus(),
                event.newStatus(),
                event.reason()
        );

        eventPublisher.publishEvent(new WebhookDeliveryRequestedEvent(webhookEventId));
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handlePaymentRefundCreated(PaymentRefundCreatedEvent event) {
        Long webhookEventId = webhookEventService.enqueuePaymentStatusUpdated(
                event.payment(),
                event.previousStatus(),
                event.newStatus(),
                event.reason()
        );

        eventPublisher.publishEvent(new WebhookDeliveryRequestedEvent(webhookEventId));
    }
}
