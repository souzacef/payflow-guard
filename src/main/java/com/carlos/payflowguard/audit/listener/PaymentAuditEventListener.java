package com.carlos.payflowguard.audit.listener;

import com.carlos.payflowguard.audit.service.AuditLogService;
import com.carlos.payflowguard.payment.event.PaymentRefundCreatedEvent;
import com.carlos.payflowguard.payment.event.PaymentStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentAuditEventListener {

    private final AuditLogService auditLogService;

    public PaymentAuditEventListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
        switch (event.source()) {
            case ADMIN_STATUS_UPDATE -> auditLogService.log(
                    "PAYMENT_STATUS_UPDATED",
                    "Payment",
                    event.payment().paymentId(),
                    event.actor(),
                    "Changed from " + event.previousStatus() + " to " + event.newStatus()
                            + (event.reason() != null ? " | Reason: " + event.reason() : "")
            );
            case ADMIN_OVERRIDE -> auditLogService.log(
                    "PAYMENT_STATUS_OVERRIDDEN",
                    "Payment",
                    event.payment().paymentId(),
                    event.actor(),
                    "Overridden from " + event.previousStatus() + " to " + event.newStatus()
                            + " | Reason: " + event.reason()
            );
            case AUTOMATIC_CAPTURE -> auditLogService.log(
                    "PAYMENT_AUTO_CAPTURED",
                    "Payment",
                    event.payment().paymentId(),
                    event.actor(),
                    "Automatically captured payment from AUTHORIZED to CAPTURED"
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentRefundCreated(PaymentRefundCreatedEvent event) {
        auditLogService.log(
                "PAYMENT_REFUND_CREATED",
                "Payment",
                event.payment().paymentId(),
                event.actor(),
                "RefundId=" + event.refundId()
                        + " | Amount=" + event.refundAmountMinor()
                        + " | TotalRefunded=" + event.totalRefundedAmountMinor()
                        + (event.reason() != null ? " | Reason: " + event.reason() : "")
        );
    }
}
