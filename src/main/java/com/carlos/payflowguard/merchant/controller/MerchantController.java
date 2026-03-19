package com.carlos.payflowguard.merchant.controller;

import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.service.MerchantService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/sample")
    public MerchantResponse getSampleMerchant() {
        return merchantService.getSampleMerchant();
    }

    @PostMapping
    public MerchantResponse createMerchant(@RequestBody CreateMerchantRequest request) {
        return merchantService.createMerchant(request);
    }
}