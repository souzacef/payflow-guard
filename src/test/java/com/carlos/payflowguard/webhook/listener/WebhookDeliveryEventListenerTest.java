package com.carlos.payflowguard.webhook.listener;

import com.carlos.payflowguard.webhook.event.WebhookDeliveryRequestedEvent;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryEventListenerTest {

    @Mock
    private WebhookEventService webhookEventService;

    @Test
    void delegatesDeliveryByPersistedEventId() {
        WebhookDeliveryEventListener listener = new WebhookDeliveryEventListener(webhookEventService);

        listener.handleWebhookDeliveryRequested(new WebhookDeliveryRequestedEvent(51L));

        verify(webhookEventService).deliverEvent(51L);
    }

    @Test
    void afterCommitFailureDoesNotEscapeAsPaymentFailure() {
        WebhookDeliveryEventListener listener = new WebhookDeliveryEventListener(webhookEventService);
        doThrow(new IllegalStateException("delivery unavailable"))
                .when(webhookEventService).deliverEvent(51L);

        assertDoesNotThrow(
                () -> listener.handleWebhookDeliveryRequested(new WebhookDeliveryRequestedEvent(51L))
        );
    }

    @Test
    void listenerIsAfterCommitWithoutFallbackExecution() throws Exception {
        TransactionalEventListener annotation = WebhookDeliveryEventListener.class
                .getMethod("handleWebhookDeliveryRequested", WebhookDeliveryRequestedEvent.class)
                .getAnnotation(TransactionalEventListener.class);

        assertNotNull(annotation);
        assertEquals(TransactionPhase.AFTER_COMMIT, annotation.phase());
        assertFalse(annotation.fallbackExecution());
    }
}
