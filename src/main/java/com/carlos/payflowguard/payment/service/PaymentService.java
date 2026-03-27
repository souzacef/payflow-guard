package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.audit.service.AuditLogService;
import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.exception.UnauthorizedException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.dto.CreatePaymentRequest;
import com.carlos.payflowguard.payment.dto.OverridePaymentStatusRequest;
import com.carlos.payflowguard.payment.dto.PaymentResponse;
import com.carlos.payflowguard.payment.dto.UpdatePaymentStatusRequest;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.user.entity.Role;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final FraudCheckService fraudCheckService;
    private final AuditLogService auditLogService;
    private final WebhookEventService webhookEventService;

    public PaymentService(
            PaymentRepository paymentRepository,
            MerchantRepository merchantRepository,
            UserRepository userRepository,
            FraudCheckService fraudCheckService,
            AuditLogService auditLogService,
            WebhookEventService webhookEventService
    ) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.fraudCheckService = fraudCheckService;
        this.auditLogService = auditLogService;
        this.webhookEventService = webhookEventService;
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof String email)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
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

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchant().getId(),
                payment.getMerchant().getBusinessName(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getDescription(),
                payment.getStatus(),
                payment.getFraudReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        User user = getAuthenticatedUser();
        Merchant merchant;

        if (user.getRole() == Role.ADMIN) {
            merchant = merchantRepository.findById(request.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Merchant not found with id: " + request.getMerchantId()
                    ));
        } else {
            merchant = merchantRepository.findByIdAndUser(request.getMerchantId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Merchant not found with id: " + request.getMerchantId()
                    ));
        }

        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot create payment for inactive merchant");
        }

        FraudCheckResult fraudCheckResult = fraudCheckService.evaluate(merchant, request.getAmountMinor());

        Payment payment = new Payment();
        payment.setMerchant(merchant);
        payment.setAmountMinor(request.getAmountMinor());
        payment.setCurrency(request.getCurrency().toUpperCase());
        payment.setDescription(request.getDescription());

        if (fraudCheckResult.isPassed()) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setFraudReason(null);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFraudReason(fraudCheckResult.getReason());
        }

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    public PageResponse<PaymentResponse> getAllPayments(
            Long merchantId,
            PaymentStatus status,
            int page,
            int size,
            String sort
    ) {
        User user = getAuthenticatedUser();
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        Page<Payment> paymentPage;

        if (user.getRole() == Role.ADMIN) {
            if (merchantId != null && status != null) {
                paymentPage = paymentRepository.findByMerchantIdAndStatus(merchantId, status, pageRequest);
            } else if (merchantId != null) {
                paymentPage = paymentRepository.findByMerchantId(merchantId, pageRequest);
            } else if (status != null) {
                paymentPage = paymentRepository.findByStatus(status, pageRequest);
            } else {
                paymentPage = paymentRepository.findAll(pageRequest);
            }
        } else {
            if (merchantId != null && status != null) {
                paymentPage = paymentRepository.findByMerchantUserAndMerchantIdAndStatus(
                        user, merchantId, status, pageRequest
                );
            } else if (merchantId != null) {
                paymentPage = paymentRepository.findByMerchantUserAndMerchantId(user, merchantId, pageRequest);
            } else if (status != null) {
                paymentPage = paymentRepository.findByMerchantUserAndStatus(user, status, pageRequest);
            } else {
                paymentPage = paymentRepository.findByMerchantUser(user, pageRequest);
            }
        }

        return new PageResponse<>(
                paymentPage.getContent().stream().map(this::toResponse).toList(),
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages()
        );
    }

    public PaymentResponse getPaymentById(Long id) {
        User user = getAuthenticatedUser();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (user.getRole() != Role.ADMIN && !payment.getMerchant().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }

        return toResponse(payment);
    }

    public PaymentResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request) {
        User user = getAuthenticatedUser();

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Unauthorized");
        }

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Only pending payments can be updated");
        }

        if (request.getStatus() == PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Invalid payment status transition");
        }

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(request.getStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        auditLogService.log(
                "PAYMENT_STATUS_UPDATED",
                "Payment",
                payment.getId(),
                user.getEmail(),
                "Changed from " + oldStatus + " to " + request.getStatus() +
                        (request.getReason() != null ? " | Reason: " + request.getReason() : "")
        );

        webhookEventService.publishPaymentStatusUpdated(
                updatedPayment,
                oldStatus,
                updatedPayment.getStatus(),
                request.getReason()
        );

        return toResponse(updatedPayment);
    }

    public PaymentResponse overridePaymentStatus(Long id, OverridePaymentStatusRequest request) {
        User user = getAuthenticatedUser();

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Unauthorized");
        }

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(request.getStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        auditLogService.log(
                "PAYMENT_STATUS_OVERRIDDEN",
                "Payment",
                payment.getId(),
                user.getEmail(),
                "Overridden from " + oldStatus + " to " + request.getStatus() +
                        " | Reason: " + request.getReason()
        );

        webhookEventService.publishPaymentStatusUpdated(
                updatedPayment,
                oldStatus,
                updatedPayment.getStatus(),
                request.getReason()
        );

        return toResponse(updatedPayment);
    }
}