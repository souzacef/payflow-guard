package com.carlos.payflowguard.merchant.controller;

import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public MerchantResponse createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        return merchantService.createMerchant(request);
    }

    @GetMapping
    public List<MerchantResponse> getAllMerchants() {
        return merchantService.getAllMerchants();
    }

    @GetMapping("/{id}")
    public MerchantResponse getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id);
    }
}