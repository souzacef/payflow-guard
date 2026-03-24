package com.carlos.payflowguard.merchant.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.exception.UnauthorizedException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private PageRequest buildPageRequest(int page, int size, String sort) {
        String[] sortParts = sort.split(",");

        String sortField = sortParts[0];
        Sort.Direction direction = Sort.Direction.ASC;

        if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        User user = getAuthenticatedUser();

        Merchant merchant = new Merchant();
        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setUser(user);

        Merchant savedMerchant = merchantRepository.save(merchant);

        return new MerchantResponse(
                savedMerchant.getId(),
                savedMerchant.getBusinessName(),
                savedMerchant.getEmail(),
                savedMerchant.getStatus(),
                savedMerchant.getCreatedAt(),
                savedMerchant.getUpdatedAt()
        );
    }

    public PageResponse<MerchantResponse> getAllMerchants(
            String email,
            String businessName,
            int page,
            int size,
            String sort
    ) {
        User user = getAuthenticatedUser();
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        Page<Merchant> merchantPage;

        if (email != null && !email.isBlank()) {
            merchantPage = merchantRepository.findByUserAndEmailContainingIgnoreCase(user, email, pageRequest);
        } else if (businessName != null && !businessName.isBlank()) {
            merchantPage = merchantRepository.findByUserAndBusinessNameContainingIgnoreCase(user, businessName, pageRequest);
        } else {
            merchantPage = merchantRepository.findByUser(user, pageRequest);
        }

        return new PageResponse<>(
                merchantPage.getContent().stream()
                        .map(merchant -> new MerchantResponse(
                                merchant.getId(),
                                merchant.getBusinessName(),
                                merchant.getEmail(),
                                merchant.getStatus(),
                                merchant.getCreatedAt(),
                                merchant.getUpdatedAt()
                        ))
                        .toList(),
                merchantPage.getNumber(),
                merchantPage.getSize(),
                merchantPage.getTotalElements(),
                merchantPage.getTotalPages()
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
                merchant.getStatus(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
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
                updatedMerchant.getStatus(),
                updatedMerchant.getCreatedAt(),
                updatedMerchant.getUpdatedAt()
        );
    }

    public void deleteMerchantById(Long id) {
        User user = getAuthenticatedUser();

        Merchant merchant = merchantRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        merchantRepository.delete(merchant);
    }
}