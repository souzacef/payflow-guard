package com.carlos.payflowguard.merchant.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.exception.UnauthorizedException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    public MerchantService(MerchantRepository merchantRepository, UserRepository userRepository) {
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof String email)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    }

    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        User user = getAuthenticatedUser();

        Merchant merchant = new Merchant();
        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());
        merchant.setStatus("ACTIVE");
        merchant.setUser(user);

        Merchant savedMerchant = merchantRepository.save(merchant);

        return new MerchantResponse(
                savedMerchant.getId(),
                savedMerchant.getBusinessName(),
                savedMerchant.getEmail(),
                savedMerchant.getStatus()
        );
    }

    public PageResponse<MerchantResponse> getAllMerchants(
            String email,
            String businessName,
            Pageable pageable
    ) {
        User user = getAuthenticatedUser();
        Page<Merchant> page;

        if (email != null && !email.isBlank()) {
            page = merchantRepository.findByUserAndEmailContainingIgnoreCase(user, email, pageable);
        } else if (businessName != null && !businessName.isBlank()) {
            page = merchantRepository.findByUserAndBusinessNameContainingIgnoreCase(user, businessName, pageable);
        } else {
            page = merchantRepository.findByUser(user, pageable);
        }

        return new PageResponse<>(
                page.getContent().stream()
                        .map(merchant -> new MerchantResponse(
                                merchant.getId(),
                                merchant.getBusinessName(),
                                merchant.getEmail(),
                                merchant.getStatus()
                        ))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public MerchantResponse getMerchantById(Long id) {
        User user = getAuthenticatedUser();

        Merchant merchant = merchantRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        return new MerchantResponse(
                merchant.getId(),
                merchant.getBusinessName(),
                merchant.getEmail(),
                merchant.getStatus()
        );
    }

    public MerchantResponse updateMerchant(Long id, CreateMerchantRequest request) {
        User user = getAuthenticatedUser();

        Merchant merchant = merchantRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());

        Merchant updatedMerchant = merchantRepository.save(merchant);

        return new MerchantResponse(
                updatedMerchant.getId(),
                updatedMerchant.getBusinessName(),
                updatedMerchant.getEmail(),
                updatedMerchant.getStatus()
        );
    }

    public void deleteMerchantById(Long id) {
        User user = getAuthenticatedUser();

        Merchant merchant = merchantRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        merchantRepository.delete(merchant);
    }
}