package com.carlos.payflowguard.audit.listener;

import com.carlos.payflowguard.audit.service.AuditLogService;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.event.PaymentEventSnapshot;
import com.carlos.payflowguard.payment.event.PaymentRefundCreatedEvent;
import com.carlos.payflowguard.payment.event.PaymentStatusChangedEvent;
import com.carlos.payflowguard.payment.event.PaymentTransitionSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentAuditEventListenerTest {

    @Mock
    private AuditLogService auditLogService;

    private PaymentAuditEventListener listener;
    private PaymentEventSnapshot payment;

    @BeforeEach
    void setUp() {
        listener = new PaymentAuditEventListener(auditLogService);
        payment = new PaymentEventSnapshot(10L, 20L, 1500L, "BRL", null);
    }

    @Test
    void mapsNormalStatusUpdateToExistingAuditFormat() {
        listener.handlePaymentStatusChanged(new PaymentStatusChangedEvent(
                payment,
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED,
                "admin@test.com",
                "Funds reserved",
                PaymentTransitionSource.ADMIN_STATUS_UPDATE
        ));

        verify(auditLogService).log(
                "PAYMENT_STATUS_UPDATED",
                "Payment",
                10L,
                "admin@test.com",
                "Changed from PENDING to AUTHORIZED | Reason: Funds reserved"
        );
    }

    @Test
    void mapsOverrideToExistingAuditFormat() {
        listener.handlePaymentStatusChanged(new PaymentStatusChangedEvent(
                payment,
                PaymentStatus.FAILED,
                PaymentStatus.AUTHORIZED,
                "admin@test.com",
                "Manual review",
                PaymentTransitionSource.ADMIN_OVERRIDE
        ));

        verify(auditLogService).log(
                "PAYMENT_STATUS_OVERRIDDEN",
                "Payment",
                10L,
                "admin@test.com",
                "Overridden from FAILED to AUTHORIZED | Reason: Manual review"
        );
    }

    @Test
    void mapsAutomaticCaptureToExistingAuditFormat() {
        listener.handlePaymentStatusChanged(new PaymentStatusChangedEvent(
                payment,
                PaymentStatus.AUTHORIZED,
                PaymentStatus.CAPTURED,
                "system",
                "Automatic capture",
                PaymentTransitionSource.AUTOMATIC_CAPTURE
        ));

        verify(auditLogService).log(
                "PAYMENT_AUTO_CAPTURED",
                "Payment",
                10L,
                "system",
                "Automatically captured payment from AUTHORIZED to CAPTURED"
        );
    }

    @Test
    void mapsRefundToExistingAuditFormat() {
        listener.handlePaymentRefundCreated(new PaymentRefundCreatedEvent(
                payment,
                30L,
                400L,
                900L,
                PaymentStatus.CAPTURED,
                PaymentStatus.CAPTURED,
                "admin@test.com",
                "Customer request"
        ));

        verify(auditLogService).log(
                "PAYMENT_REFUND_CREATED",
                "Payment",
                10L,
                "admin@test.com",
                "RefundId=30 | Amount=400 | TotalRefunded=900 | Reason: Customer request"
        );
    }
}
