package com.carlos.payflowguard.webhook.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.webhook.dto.WebhookEventResponse;
import com.carlos.payflowguard.webhook.entity.WebhookEvent;
import com.carlos.payflowguard.webhook.entity.WebhookEventStatus;
import com.carlos.payflowguard.webhook.repository.WebhookEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

@Service
public class WebhookEventService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final WebhookEventRepository webhookEventRepository;
    private final HttpClient httpClient;
    private final String paymentStatusUrl;

    public WebhookEventService(
            WebhookEventRepository webhookEventRepository,
            @Value("${app.webhooks.payment-status-url}") String paymentStatusUrl
    ) {
        this.webhookEventRepository = webhookEventRepository;
        this.paymentStatusUrl = paymentStatusUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void publishPaymentStatusUpdated(
            Payment payment,
            PaymentStatus oldStatus,
            PaymentStatus newStatus,
            String reason
    ) {
        String payload = buildPaymentStatusPayload(payment, oldStatus, newStatus, reason);

        WebhookEvent event = new WebhookEvent();
        event.setEventType("payment.status.updated");
        event.setEntityName("Payment");
        event.setEntityId(payment.getId());
        event.setPayload(payload);
        event.setTargetUrl(paymentStatusUrl);

        WebhookEvent savedEvent = webhookEventRepository.save(event);

        deliver(savedEvent);
    }

    public PageResponse<WebhookEventResponse> getAllEvents(int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        Page<WebhookEvent> eventsPage = webhookEventRepository.findAll(pageRequest);

        return new PageResponse<>(
                eventsPage.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                eventsPage.getNumber(),
                eventsPage.getSize(),
                eventsPage.getTotalElements(),
                eventsPage.getTotalPages()
        );
    }

    public WebhookEventResponse getEventById(Long id) {
        WebhookEvent event = webhookEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook event not found with id: " + id));

        return toResponse(event);
    }

    public WebhookEventResponse retryEvent(Long id) {
        WebhookEvent event = webhookEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook event not found with id: " + id));

        if (event.getStatus() == WebhookEventStatus.SENT) {
            throw new IllegalArgumentException("Webhook event has already been sent");
        }

        if (event.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("Webhook event reached maximum retry attempts");
        }

        deliver(event);

        return toResponse(event);
    }

    @Scheduled(fixedDelay = 30000)
    public void retryFailedEvents() {
        List<WebhookEvent> failedEvents = webhookEventRepository.findByStatusAndAttemptCountLessThan(
                WebhookEventStatus.FAILED,
                MAX_ATTEMPTS
        );

        for (WebhookEvent event : failedEvents) {
            deliver(event);
        }
    }

    private void deliver(WebhookEvent event) {
        event.setAttemptCount(event.getAttemptCount() + 1);
        event.setLastAttemptAt(Instant.now());

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(event.getTargetUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(event.getPayload()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            event.setResponseStatusCode(response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                event.setStatus(WebhookEventStatus.SENT);
                event.setLastError(null);
            } else {
                event.setStatus(WebhookEventStatus.FAILED);
                event.setLastError(truncate("HTTP " + response.statusCode() + ": " + response.body()));
            }

        } catch (IOException | InterruptedException ex) {
            event.setStatus(WebhookEventStatus.FAILED);
            event.setResponseStatusCode(null);
            event.setLastError(truncate(ex.getMessage()));

            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        webhookEventRepository.save(event);
    }

    private WebhookEventResponse toResponse(WebhookEvent event) {
        return new WebhookEventResponse(
                event.getId(),
                event.getEventType(),
                event.getEntityName(),
                event.getEntityId(),
                event.getPayload(),
                event.getTargetUrl(),
                event.getResponseStatusCode(),
                event.getLastError(),
                event.getStatus(),
                event.getAttemptCount(),
                event.getLastAttemptAt(),
                event.getCreatedAt()
        );
    }

    private PageRequest buildPageRequest(int page, int size, String sort) {
        String[] sortParts = sort.split(",");

        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.ASC;

        if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    private String buildPaymentStatusPayload(
            Payment payment,
            PaymentStatus oldStatus,
            PaymentStatus newStatus,
            String reason
    ) {
        String safeReason = reason == null ? "" : escapeJson(reason);
        String fraudReason = payment.getFraudReason() == null ? "" : escapeJson(payment.getFraudReason());

        return """
                {
                  "paymentId": %d,
                  "merchantId": %d,
                  "oldStatus": "%s",
                  "newStatus": "%s",
                  "amountMinor": %d,
                  "currency": "%s",
                  "reason": "%s",
                  "fraudReason": "%s"
                }
                """.formatted(
                payment.getId(),
                payment.getMerchant().getId(),
                oldStatus.name(),
                newStatus.name(),
                payment.getAmountMinor(),
                escapeJson(payment.getCurrency()),
                safeReason,
                fraudReason
        );
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }

        if (value.length() <= MAX_ERROR_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_ERROR_LENGTH);
    }
}