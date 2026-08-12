package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAutoCaptureServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @Test
    void scheduledBatchDelegatesEachEligiblePaymentIndividually() {
        Payment first = new Payment();
        first.setId(11L);
        Payment second = new Payment();
        second.setId(12L);

        when(paymentRepository.findByStatus(PaymentStatus.AUTHORIZED))
                .thenReturn(List.of(first, second));

        PaymentAutoCaptureService service =
                new PaymentAutoCaptureService(paymentRepository, paymentService);

        service.autoCaptureAuthorizedPayments();

        verify(paymentService).captureAutomatically(11L);
        verify(paymentService).captureAutomatically(12L);
    }
}
