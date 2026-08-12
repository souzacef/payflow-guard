package com.carlos.payflowguard.webhook.service;

import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.event.PaymentEventSnapshot;
import com.carlos.payflowguard.webhook.entity.WebhookEvent;
import com.carlos.payflowguard.webhook.entity.WebhookEventStatus;
import com.carlos.payflowguard.webhook.repository.WebhookEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookEventServiceTest {

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private HttpClient httpClient;

    @Test
    void enqueuePersistsExistingPayloadWithoutPerformingHttp() {
        WebhookEvent savedEvent = mock(WebhookEvent.class);
        when(savedEvent.getId()).thenReturn(51L);
        when(webhookEventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> {
            WebhookEvent event = invocation.getArgument(0);
            event.prePersist();
            return savedEvent;
        });

        WebhookEventService service =
                new WebhookEventService(webhookEventRepository, "https://example.test/webhook", httpClient);

        Long id = service.enqueuePaymentStatusUpdated(
                new PaymentEventSnapshot(10L, 20L, 1500L, "BRL", "Manual \"review\""),
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED,
                "Funds \"reserved\""
        );

        assertEquals(51L, id);

        ArgumentCaptor<WebhookEvent> captor = ArgumentCaptor.forClass(WebhookEvent.class);
        verify(webhookEventRepository).save(captor.capture());
        WebhookEvent event = captor.getValue();

        assertEquals("payment.status.updated", event.getEventType());
        assertEquals("Payment", event.getEntityName());
        assertEquals(10L, event.getEntityId());
        assertEquals("https://example.test/webhook", event.getTargetUrl());
        assertEquals(WebhookEventStatus.PENDING, event.getStatus());
        assertEquals(0, event.getAttemptCount());
        assertTrue(event.getPayload().contains("\"paymentId\": 10"));
        assertTrue(event.getPayload().contains("\"merchantId\": 20"));
        assertTrue(event.getPayload().contains("\"oldStatus\": \"PENDING\""));
        assertTrue(event.getPayload().contains("\"newStatus\": \"AUTHORIZED\""));
        assertTrue(event.getPayload().contains("\"amountMinor\": 1500"));
        assertTrue(event.getPayload().contains("\"currency\": \"BRL\""));
        assertTrue(event.getPayload().contains("\"reason\": \"Funds \\\"reserved\\\"\""));
        assertTrue(event.getPayload().contains("\"fraudReason\": \"Manual \\\"review\\\"\""));
        verifyNoInteractions(httpClient);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void failedHttpDeliveryPersistsRetryableResult() throws Exception {
        WebhookEvent event = pendingEvent();
        HttpResponse<String> response = mock(HttpResponse.class);

        when(webhookEventRepository.findById(51L)).thenReturn(Optional.of(event));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) response);
        when(response.statusCode()).thenReturn(503);
        when(response.body()).thenReturn("temporarily unavailable");

        WebhookEventService service =
                new WebhookEventService(webhookEventRepository, "https://example.test/webhook", httpClient);

        service.deliverEvent(51L);

        assertEquals(WebhookEventStatus.FAILED, event.getStatus());
        assertEquals(1, event.getAttemptCount());
        assertEquals(503, event.getResponseStatusCode());
        assertEquals("HTTP 503: temporarily unavailable", event.getLastError());
        assertNotNull(event.getLastAttemptAt());
        verify(webhookEventRepository).save(event);
    }

    @Test
    void deliveryUsesIndependentTransaction() throws Exception {
        Transactional annotation = WebhookEventService.class
                .getMethod("deliverEvent", Long.class)
                .getAnnotation(Transactional.class);

        assertNotNull(annotation);
        assertEquals(Propagation.REQUIRES_NEW, annotation.propagation());
    }

    private WebhookEvent pendingEvent() {
        WebhookEvent event = new WebhookEvent();
        event.setEventType("payment.status.updated");
        event.setEntityName("Payment");
        event.setEntityId(10L);
        event.setPayload("{}");
        event.setTargetUrl("https://example.test/webhook");
        event.prePersist();
        return event;
    }
}
