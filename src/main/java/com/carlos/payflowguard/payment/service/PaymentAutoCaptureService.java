package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentAutoCaptureService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentAutoCaptureService(
            PaymentRepository paymentRepository,
            PaymentService paymentService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelay = 30000)
    public void autoCaptureAuthorizedPayments() {
        List<Payment> authorizedPayments = paymentRepository.findByStatus(PaymentStatus.AUTHORIZED);

        for (Payment payment : authorizedPayments) {
            paymentService.captureAutomatically(payment.getId());
        }
    }
}
