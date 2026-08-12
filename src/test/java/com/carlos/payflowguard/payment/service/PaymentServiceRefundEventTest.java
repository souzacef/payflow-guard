package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.dto.RefundPaymentRequest;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.entity.Refund;
import com.carlos.payflowguard.payment.event.PaymentRefundCreatedEvent;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceRefundEventTest {

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

        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);

        Merchant merchant = new Merchant(2L, "Merchant", "merchant@test.com", MerchantStatus.ACTIVE, admin);

        payment = new Payment();
        payment.setId(3L);
        payment.setMerchant(merchant);
        payment.setAmountMinor(1000L);
        payment.setRefundedAmountMinor(0L);
        payment.setCurrency("BRL");
        payment.setStatus(PaymentStatus.CAPTURED);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), null)
        );

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            ReflectionTestUtils.setField(refund, "id", 30L);
            return refund;
        });
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void partialRefundPublishesOneRefundEventWithoutStatusEvent() {
        paymentService.refundPayment(payment.getId(), refundRequest(400L, "Partial refund"));

        PaymentRefundCreatedEvent event = publishedRefundEvent();

        assertEquals(30L, event.refundId());
        assertEquals(400L, event.refundAmountMinor());
        assertEquals(400L, event.totalRefundedAmountMinor());
        assertEquals(PaymentStatus.CAPTURED, event.previousStatus());
        assertEquals(PaymentStatus.CAPTURED, event.newStatus());
        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
    }

    @Test
    void fullRefundPublishesOneRefundEventWithoutDuplicateStatusEvent() {
        paymentService.refundPayment(payment.getId(), refundRequest(1000L, "Full refund"));

        PaymentRefundCreatedEvent event = publishedRefundEvent();

        assertEquals(1000L, event.refundAmountMinor());
        assertEquals(1000L, event.totalRefundedAmountMinor());
        assertEquals(PaymentStatus.CAPTURED, event.previousStatus());
        assertEquals(PaymentStatus.REFUNDED, event.newStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }

    private PaymentRefundCreatedEvent publishedRefundEvent() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertInstanceOf(PaymentRefundCreatedEvent.class, captor.getValue());
        return (PaymentRefundCreatedEvent) captor.getValue();
    }

    private RefundPaymentRequest refundRequest(Long amountMinor, String reason) {
        RefundPaymentRequest request = new RefundPaymentRequest();
        request.setAmountMinor(amountMinor);
        request.setReason(reason);
        return request;
    }
}
