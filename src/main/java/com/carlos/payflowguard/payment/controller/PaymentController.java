package com.carlos.payflowguard.payment.controller;

import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.payment.dto.CreatePaymentRequest;
import com.carlos.payflowguard.payment.dto.OverridePaymentStatusRequest;
import com.carlos.payflowguard.payment.dto.PaymentResponse;
import com.carlos.payflowguard.payment.dto.RefundPaymentRequest;
import com.carlos.payflowguard.payment.dto.UpdatePaymentStatusRequest;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping
    public PageResponse<PaymentResponse> getAllPayments(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        return paymentService.getAllPayments(merchantId, status, page, size, sort);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @PatchMapping("/{id}/status")
    public PaymentResponse updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request
    ) {
        return paymentService.updatePaymentStatus(id, request);
    }

    @PatchMapping("/{id}/override-status")
    public PaymentResponse overridePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody OverridePaymentStatusRequest request
    ) {
        return paymentService.overridePaymentStatus(id, request);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundPaymentRequest request
    ) {
        return ResponseEntity.ok(paymentService.refundPayment(id, request));
    }
}