package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.dto.OverridePaymentStatusRequest;
import com.carlos.payflowguard.payment.dto.UpdatePaymentStatusRequest;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.event.PaymentStatusChangedEvent;
import com.carlos.payflowguard.payment.event.PaymentTransitionSource;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.payment.repository.RefundRepository;
import com.carlos.payflowguard.user.entity.Role;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceEventPublishingTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FraudCheckService fraudCheckService;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentService paymentService;
    private User admin;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                merchantRepository,
                userRepository,
                fraudCheckService,
                refundRepository,
                eventPublisher
        );

        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);

        Merchant merchant = new Merchant(2L, "Merchant", "merchant@test.com", MerchantStatus.ACTIVE, admin);

        payment = new Payment();
        payment.setId(3L);
        payment.setMerchant(merchant);
        payment.setAmountMinor(1500L);
        payment.setRefundedAmountMinor(0L);
        payment.setCurrency("BRL");
        payment.setFraudReason("reviewed");
        payment.setStatus(PaymentStatus.PENDING);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), null)
        );

        lenient().when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        lenient().when(paymentRepository.save(payment)).thenReturn(payment);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pendingToAuthorizedPublishesExactlyOneImmutableEvent() {
        UpdatePaymentStatusRequest request = statusUpdate(PaymentStatus.AUTHORIZED, "Funds reserved");

        paymentService.updatePaymentStatus(payment.getId(), request);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());

        PaymentStatusChangedEvent event = (PaymentStatusChangedEvent) captor.getValue();
        assertEquals(3L, event.payment().paymentId());
        assertEquals(2L, event.payment().merchantId());
        assertEquals(1500L, event.payment().amountMinor());
        assertEquals("BRL", event.payment().currency());
        assertEquals("reviewed", event.payment().fraudReason());
        assertEquals(PaymentStatus.PENDING, event.previousStatus());
        assertEquals(PaymentStatus.AUTHORIZED, event.newStatus());
        assertEquals("admin@test.com", event.actor());
        assertEquals("Funds reserved", event.reason());
        assertEquals(PaymentTransitionSource.ADMIN_STATUS_UPDATE, event.source());
    }

    @Test
    void authorizedToCapturedPublishesExactlyOneStatusEvent() {
        payment.setStatus(PaymentStatus.AUTHORIZED);

        paymentService.updatePaymentStatus(
                payment.getId(),
                statusUpdate(PaymentStatus.CAPTURED, "Settlement completed")
        );

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PaymentStatusChangedEvent event = (PaymentStatusChangedEvent) captor.getValue();

        assertEquals(PaymentStatus.AUTHORIZED, event.previousStatus());
        assertEquals(PaymentStatus.CAPTURED, event.newStatus());
    }

    @Test
    void overridePublishesOverrideSource() {
        OverridePaymentStatusRequest request = new OverridePaymentStatusRequest();
        request.setStatus(PaymentStatus.FAILED);
        request.setReason("Manual review");

        paymentService.overridePaymentStatus(payment.getId(), request);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PaymentStatusChangedEvent event = (PaymentStatusChangedEvent) captor.getValue();

        assertEquals(PaymentTransitionSource.ADMIN_OVERRIDE, event.source());
        assertEquals(PaymentStatus.PENDING, event.previousStatus());
        assertEquals(PaymentStatus.FAILED, event.newStatus());
    }

    @Test
    void invalidTransitionPublishesNoEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus(
                        payment.getId(),
                        statusUpdate(PaymentStatus.CAPTURED, "Skip authorization")
                )
        );

        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unchangedTransitionPublishesNoEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus(
                        payment.getId(),
                        statusUpdate(PaymentStatus.PENDING, "No change")
                )
        );

        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void automaticCapturePublishesAutomaticSourceWithoutAuthentication() {
        SecurityContextHolder.clearContext();
        payment.setStatus(PaymentStatus.AUTHORIZED);

        paymentService.captureAutomatically(payment.getId());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PaymentStatusChangedEvent event = (PaymentStatusChangedEvent) captor.getValue();

        assertEquals(PaymentTransitionSource.AUTOMATIC_CAPTURE, event.source());
        assertEquals("system", event.actor());
        assertEquals("Automatic capture", event.reason());
        assertEquals(PaymentStatus.AUTHORIZED, event.previousStatus());
        assertEquals(PaymentStatus.CAPTURED, event.newStatus());
    }

    private UpdatePaymentStatusRequest statusUpdate(PaymentStatus status, String reason) {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(status);
        request.setReason(reason);
        return request;
    }
}
