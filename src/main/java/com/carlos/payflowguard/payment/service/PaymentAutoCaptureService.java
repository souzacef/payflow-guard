package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.audit.service.AuditLogService;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentAutoCaptureService {

    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;
    private final WebhookEventService webhookEventService;

    public PaymentAutoCaptureService(
            PaymentRepository paymentRepository,
            AuditLogService auditLogService,
            WebhookEventService webhookEventService
    ) {
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
        this.webhookEventService = webhookEventService;
    }

    @Scheduled(fixedDelay = 30000)
    public void autoCaptureAuthorizedPayments() {
        List<Payment> authorizedPayments = paymentRepository.findByStatus(PaymentStatus.AUTHORIZED);

        for (Payment payment : authorizedPayments) {
            PaymentStatus oldStatus = payment.getStatus();

            if (oldStatus != PaymentStatus.AUTHORIZED) {
                continue;
            }

            payment.setStatus(PaymentStatus.CAPTURED);
            Payment updatedPayment = paymentRepository.save(payment);

            auditLogService.log(
                    "PAYMENT_AUTO_CAPTURED",
                    "Payment",
                    updatedPayment.getId(),
                    "system",
                    "Automatically captured payment from AUTHORIZED to CAPTURED"
            );

            webhookEventService.publishPaymentStatusUpdated(
                    updatedPayment,
                    oldStatus,
                    updatedPayment.getStatus(),
                    "Automatic capture"
            );
        }
    }
}