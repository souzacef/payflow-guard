package com.carlos.payflowguard.webhook.listener;

import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.event.PaymentEventSnapshot;
import com.carlos.payflowguard.payment.event.PaymentRefundCreatedEvent;
import com.carlos.payflowguard.payment.event.PaymentStatusChangedEvent;
import com.carlos.payflowguard.payment.event.PaymentTransitionSource;
import com.carlos.payflowguard.webhook.event.WebhookDeliveryRequestedEvent;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookEventListenerTest {

    @Mock
    private WebhookEventService webhookEventService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentWebhookEventListener listener;
    private PaymentEventSnapshot payment;

    @BeforeEach
    void setUp() {
        listener = new PaymentWebhookEventListener(webhookEventService, eventPublisher);
        payment = new PaymentEventSnapshot(10L, 20L, 1500L, "BRL", "reviewed");
    }

    @Test
    void statusEventEnqueuesOneWebhookAndPublishesOneDeliveryRequest() {
        when(webhookEventService.enqueuePaymentStatusUpdated(
                payment,
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED,
                "Funds reserved"
        )).thenReturn(41L);

        listener.handlePaymentStatusChanged(new PaymentStatusChangedEvent(
                payment,
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED,
                "admin@test.com",
                "Funds reserved",
                PaymentTransitionSource.ADMIN_STATUS_UPDATE
        ));

        verify(webhookEventService).enqueuePaymentStatusUpdated(
                payment,
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED,
                "Funds reserved"
        );

        ArgumentCaptor<WebhookDeliveryRequestedEvent> captor =
                ArgumentCaptor.forClass(WebhookDeliveryRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(41L, captor.getValue().webhookEventId());
    }

    @Test
    void partialRefundPreservesCapturedToCapturedWebhook() {
        when(webhookEventService.enqueuePaymentStatusUpdated(
                payment,
                PaymentStatus.CAPTURED,
                PaymentStatus.CAPTURED,
                "Partial refund"
        )).thenReturn(42L);

        listener.handlePaymentRefundCreated(new PaymentRefundCreatedEvent(
                payment,
                30L,
                400L,
                400L,
                PaymentStatus.CAPTURED,
                PaymentStatus.CAPTURED,
                "admin@test.com",
                "Partial refund"
        ));

        verify(webhookEventService).enqueuePaymentStatusUpdated(
                payment,
                PaymentStatus.CAPTURED,
                PaymentStatus.CAPTURED,
                "Partial refund"
        );
        verify(eventPublisher).publishEvent(new WebhookDeliveryRequestedEvent(42L));
    }

    @Test
    void fullRefundPreservesCapturedToRefundedWebhook() {
        when(webhookEventService.enqueuePaymentStatusUpdated(
                payment,
                PaymentStatus.CAPTURED,
                PaymentStatus.REFUNDED,
                "Full refund"
        )).thenReturn(43L);

        listener.handlePaymentRefundCreated(new PaymentRefundCreatedEvent(
                payment,
                31L,
                1500L,
                1500L,
                PaymentStatus.CAPTURED,
                PaymentStatus.REFUNDED,
                "admin@test.com",
                "Full refund"
        ));

        verify(webhookEventService).enqueuePaymentStatusUpdated(
                payment,
                PaymentStatus.CAPTURED,
                PaymentStatus.REFUNDED,
                "Full refund"
        );
        verify(eventPublisher).publishEvent(new WebhookDeliveryRequestedEvent(43L));
    }
}
