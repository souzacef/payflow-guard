package com.carlos.payflowguard.merchant.repository;

import com.carlos.payflowguard.merchant.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Page<Merchant> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Merchant> findByBusinessNameContainingIgnoreCase(String businessName, Pageable pageable);
}