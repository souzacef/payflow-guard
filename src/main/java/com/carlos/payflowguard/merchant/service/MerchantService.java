package com.carlos.payflowguard.merchant.service;

import com.carlos.payflowguard.common.exception.ResourceNotFoundException;
import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public MerchantResponse getSampleMerchant() {
        return new MerchantResponse(
                1L,
                "Carlos Payments LTDA",
                "contato@payflowguard.com",
                "ACTIVE"
        );
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

    public List<MerchantResponse> getAllMerchants() {
        return merchantRepository.findAll()
                .stream()
                .map(merchant -> new MerchantResponse(
                        merchant.getId(),
                        merchant.getBusinessName(),
                        merchant.getEmail(),
                        merchant.getStatus()
                ))
                .collect(Collectors.toList());
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

    public void deleteMerchantById(Long id) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));

        merchantRepository.delete(merchant);
    }
}