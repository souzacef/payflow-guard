package com.carlos.payflowguard.webhook.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.webhook.dto.WebhookEventResponse;
import com.carlos.payflowguard.webhook.entity.WebhookEvent;
import com.carlos.payflowguard.webhook.repository.WebhookEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;

    public WebhookEventService(WebhookEventRepository webhookEventRepository) {
        this.webhookEventRepository = webhookEventRepository;
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

        webhookEventRepository.save(event);
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

    private WebhookEventResponse toResponse(WebhookEvent event) {
        return new WebhookEventResponse(
                event.getId(),
                event.getEventType(),
                event.getEntityName(),
                event.getEntityId(),
                event.getPayload(),
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
}