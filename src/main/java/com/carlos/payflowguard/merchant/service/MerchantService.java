package com.carlos.payflowguard.merchant.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        Merchant merchant = new Merchant();
        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());
        merchant.setStatus("ACTIVE");

        Merchant savedMerchant = merchantRepository.save(merchant);

        return new MerchantResponse(
                savedMerchant.getId(),
                savedMerchant.getBusinessName(),
                savedMerchant.getEmail(),
                savedMerchant.getStatus()
        );
    }

    public PageResponse<MerchantResponse> getAllMerchants(Pageable pageable) {
        Page<Merchant> page = merchantRepository.findAll(pageable);

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
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        return new MerchantResponse(
                merchant.getId(),
                merchant.getBusinessName(),
                merchant.getEmail(),
                merchant.getStatus()
        );
    }

    public MerchantResponse updateMerchant(Long id, CreateMerchantRequest request) {
        Merchant merchant = merchantRepository.findById(id)
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
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        merchantRepository.delete(merchant);
    }
}