package com.carlos.payflowguard.merchant.controller;

import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    @GetMapping("/sample")
    public MerchantResponse getSampleMerchant() {
        return new MerchantResponse(
                1L,
                "Carlos Payments LTDA",
                "contato@payflowguard.com",
                "ACTIVE"
        );
    }
}