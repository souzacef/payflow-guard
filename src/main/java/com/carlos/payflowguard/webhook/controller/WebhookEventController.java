package com.carlos.payflowguard.webhook.controller;

import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.webhook.dto.WebhookEventResponse;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook-events")
public class WebhookEventController {

    private final WebhookEventService webhookEventService;

    public WebhookEventController(WebhookEventService webhookEventService) {
        this.webhookEventService = webhookEventService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<WebhookEventResponse> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        return webhookEventService.getAllEvents(page, size, sort);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public WebhookEventResponse getEventById(@PathVariable Long id) {
        return webhookEventService.getEventById(id);
    }
}