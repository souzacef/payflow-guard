package com.carlos.payflowguard.merchant.controller;

import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public MerchantResponse createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        return merchantService.createMerchant(request);
    }

    @GetMapping
    public PageResponse<MerchantResponse> getAllMerchants(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String businessName,
            Pageable pageable
    ) {
        return merchantService.getAllMerchants(email, businessName, pageable);
    }

    @GetMapping("/{id}")
    public MerchantResponse getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id);
    }

    @PutMapping("/{id}")
    public MerchantResponse updateMerchant(
            @PathVariable Long id,
            @Valid @RequestBody CreateMerchantRequest request
    ) {
        return merchantService.updateMerchant(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchantById(@PathVariable Long id) {
        merchantService.deleteMerchantById(id);
        return ResponseEntity.noContent().build();
    }
}