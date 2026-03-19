package com.carlos.payflowguard.merchant.service;

import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    public MerchantResponse getSampleMerchant() {
        return new MerchantResponse(
                1L,
                "Carlos Payments LTDA",
                "contato@payflowguard.com",
                "ACTIVE"
        );
    }

    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        // Fake creation (no database yet)

        return new MerchantResponse(
                2L,
                request.getBusinessName(),
                request.getEmail(),
                "ACTIVE"
        );
    }
}