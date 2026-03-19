package com.carlos.payflowguard.merchant.service;

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
}