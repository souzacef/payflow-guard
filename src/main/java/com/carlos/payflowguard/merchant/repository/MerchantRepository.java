package com.carlos.payflowguard.merchant.repository;

import com.carlos.payflowguard.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
}