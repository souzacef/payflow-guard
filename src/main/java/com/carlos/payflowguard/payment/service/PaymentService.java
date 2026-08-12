package com.carlos.payflowguard.payment.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.exception.UnauthorizedException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.dto.CreatePaymentRequest;
import com.carlos.payflowguard.payment.dto.OverridePaymentStatusRequest;
import com.carlos.payflowguard.payment.dto.PaymentResponse;
import com.carlos.payflowguard.payment.dto.RefundPaymentRequest;
import com.carlos.payflowguard.payment.dto.RefundResponse;
import com.carlos.payflowguard.payment.dto.UpdatePaymentStatusRequest;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.entity.Refund;
import com.carlos.payflowguard.payment.event.PaymentEventSnapshot;
import com.carlos.payflowguard.payment.event.PaymentRefundCreatedEvent;
import com.carlos.payflowguard.payment.event.PaymentStatusChangedEvent;
import com.carlos.payflowguard.payment.event.PaymentTransitionSource;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.payment.repository.RefundRepository;
import com.carlos.payflowguard.user.entity.Role;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final FraudCheckService fraudCheckService;
    private final RefundRepository refundRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            MerchantRepository merchantRepository,
            UserRepository userRepository,
            FraudCheckService fraudCheckService,
            RefundRepository refundRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.fraudCheckService = fraudCheckService;
        this.refundRepository = refundRepository;
        this.eventPublisher = eventPublisher;
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
                payment.getRefundedAmountMinor(),
                payment.getCurrency(),
                payment.getDescription(),
                payment.getStatus(),
                payment.getFraudReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private RefundResponse toRefundResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getPayment().getId(),
                refund.getAmountMinor(),
                refund.getReason(),
                refund.getCreatedAt()
        );
    }

    private void ensureAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    private boolean isValidLifecycleTransition(PaymentStatus from, PaymentStatus to) {
        return switch (from) {
            case PENDING -> to == PaymentStatus.AUTHORIZED || to == PaymentStatus.FAILED;
            case AUTHORIZED -> to == PaymentStatus.CAPTURED || to == PaymentStatus.FAILED;
            case CAPTURED -> false;
            case FAILED, REFUNDED -> false;
        };
    }

    public PaymentResponse createPayment(CreatePaymentRequest request, String idempotencyKey) {
        User user = getAuthenticatedUser();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        String normalizedIdempotencyKey = idempotencyKey.trim();
        Merchant merchant;

        if (user.getRole() == Role.ADMIN) {
            merchant = merchantRepository.findById(request.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Merchant not found with id: " + request.getMerchantId()
                    ));

            Payment existingPayment = paymentRepository
                    .findByMerchantIdAndIdempotencyKey(merchant.getId(), normalizedIdempotencyKey)
                    .orElse(null);

            if (existingPayment != null) {
                return toResponse(existingPayment);
            }
        } else {
            merchant = merchantRepository.findByIdAndUser(request.getMerchantId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Merchant not found with id: " + request.getMerchantId()
                    ));

            Payment existingPayment = paymentRepository
                    .findByMerchantUserAndMerchantIdAndIdempotencyKey(user, merchant.getId(), normalizedIdempotencyKey)
                    .orElse(null);

            if (existingPayment != null) {
                return toResponse(existingPayment);
            }
        }

        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot create payment for inactive merchant");
        }

        FraudCheckResult fraudCheckResult = fraudCheckService.evaluate(merchant, request.getAmountMinor());

        Payment payment = new Payment();
        payment.setMerchant(merchant);
        payment.setAmountMinor(request.getAmountMinor());
        payment.setRefundedAmountMinor(0L);
        payment.setCurrency(request.getCurrency().toUpperCase());
        payment.setDescription(request.getDescription());
        payment.setIdempotencyKey(normalizedIdempotencyKey);

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

    public List<RefundResponse> getRefundsByPaymentId(Long paymentId) {
        User user = getAuthenticatedUser();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (user.getRole() != Role.ADMIN && !payment.getMerchant().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Payment not found with id: " + paymentId);
        }

        return refundRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId)
                .stream()
                .map(this::toRefundResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request) {
        User user = getAuthenticatedUser();
        ensureAdmin(user);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        PaymentStatus oldStatus = payment.getStatus();
        PaymentStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            throw new IllegalArgumentException("Status is already " + newStatus);
        }

        if (!isValidLifecycleTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid payment status transition from " + oldStatus + " to " + newStatus
            );
        }

        payment.setStatus(newStatus);

        Payment updatedPayment = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentStatusChangedEvent(
                toEventSnapshot(updatedPayment),
                oldStatus,
                updatedPayment.getStatus(),
                user.getEmail(),
                request.getReason(),
                PaymentTransitionSource.ADMIN_STATUS_UPDATE
        ));

        return toResponse(updatedPayment);
    }

    @Transactional
    public PaymentResponse overridePaymentStatus(Long id, OverridePaymentStatusRequest request) {
        User user = getAuthenticatedUser();
        ensureAdmin(user);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        PaymentStatus oldStatus = payment.getStatus();

        if (oldStatus == request.getStatus()) {
            throw new IllegalArgumentException("Status is already " + request.getStatus());
        }

        payment.setStatus(request.getStatus());

        Payment updatedPayment = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentStatusChangedEvent(
                toEventSnapshot(updatedPayment),
                oldStatus,
                updatedPayment.getStatus(),
                user.getEmail(),
                request.getReason(),
                PaymentTransitionSource.ADMIN_OVERRIDE
        ));

        return toResponse(updatedPayment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long id, RefundPaymentRequest request) {
        User user = getAuthenticatedUser();
        ensureAdmin(user);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only captured payments can be refunded");
        }

        long totalAmount = payment.getAmountMinor();
        long alreadyRefunded = payment.getRefundedAmountMinor();
        long remainingAmount = totalAmount - alreadyRefunded;
        long requestedAmount = request.getAmountMinor();

        if (remainingAmount == 0) {
            throw new IllegalStateException("Payment is already fully refunded");
        }

        if (requestedAmount <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }

        if (requestedAmount > remainingAmount) {
            throw new IllegalStateException("Refund amount exceeds remaining captured amount");
        }

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmountMinor(requestedAmount);
        refund.setReason(request.getReason());

        refundRepository.save(refund);

        long newRefundedAmount = alreadyRefunded + requestedAmount;
        PaymentStatus oldStatus = payment.getStatus();

        payment.setRefundedAmountMinor(newRefundedAmount);

        if (newRefundedAmount == totalAmount) {
            payment.setStatus(PaymentStatus.REFUNDED);
        }

        Payment updatedPayment = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentRefundCreatedEvent(
                toEventSnapshot(updatedPayment),
                refund.getId(),
                requestedAmount,
                newRefundedAmount,
                oldStatus,
                updatedPayment.getStatus(),
                user.getEmail(),
                request.getReason()
        ));

        return toResponse(updatedPayment);
    }

    @Transactional
    public void captureAutomatically(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        PaymentStatus oldStatus = payment.getStatus();

        if (oldStatus != PaymentStatus.AUTHORIZED) {
            return;
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        Payment updatedPayment = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentStatusChangedEvent(
                toEventSnapshot(updatedPayment),
                oldStatus,
                updatedPayment.getStatus(),
                "system",
                "Automatic capture",
                PaymentTransitionSource.AUTOMATIC_CAPTURE
        ));
    }

    private PaymentEventSnapshot toEventSnapshot(Payment payment) {
        return new PaymentEventSnapshot(
                payment.getId(),
                payment.getMerchant().getId(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getFraudReason()
        );
    }
}
