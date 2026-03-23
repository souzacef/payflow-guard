package com.carlos.payflowguard.merchant.repository;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Page<Merchant> findByUser(User user, Pageable pageable);

    Page<Merchant> findByUserAndEmailContainingIgnoreCase(User user, String email, Pageable pageable);

    Page<Merchant> findByUserAndBusinessNameContainingIgnoreCase(User user, String businessName, Pageable pageable);

    Optional<Merchant> findByIdAndUser(Long id, User user);
}