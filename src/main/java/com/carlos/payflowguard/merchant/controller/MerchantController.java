package com.carlos.payflowguard.merchant.controller;

import com.carlos.payflowguard.common.response.PageResponse;
import com.carlos.payflowguard.merchant.dto.CreateMerchantRequest;
import com.carlos.payflowguard.merchant.dto.MerchantResponse;
import com.carlos.payflowguard.merchant.dto.UpdateMerchantStatusRequest;
import com.carlos.payflowguard.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public PageResponse<MerchantResponse> getAllMerchants(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String businessName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        return merchantService.getAllMerchants(email, businessName, page, size, sort);
    }

    @GetMapping("/{id}")
    public MerchantResponse getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id);
    }

    @PostMapping
    public MerchantResponse createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        return merchantService.createMerchant(request);
    }

    @PutMapping("/{id}")
    public MerchantResponse updateMerchant(
            @PathVariable Long id,
            @Valid @RequestBody CreateMerchantRequest request
    ) {
        return merchantService.updateMerchant(id, request);
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public MerchantResponse updateMerchantStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMerchantStatusRequest request
    ) {
        return merchantService.updateMerchantStatus(id, request);
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchantById(id);
    }
}