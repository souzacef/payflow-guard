package com.carlos.payflowguard.payment;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.user.entity.Role;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private Merchant merchant;
    private Payment payment;

    @BeforeEach
    void setup() {
        paymentRepository.deleteAll();
        merchantRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setEmail("admin-lifecycle@test.com");
        admin.setPassword("123");
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        merchant = new Merchant();
        merchant.setBusinessName("Lifecycle Merchant");
        merchant.setEmail("lifecycle-merchant@test.com");
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setUser(admin);
        merchant = merchantRepository.save(merchant);

        payment = new Payment();
        payment.setMerchant(merchant);
        payment.setAmountMinor(1000L);
        payment.setRefundedAmountMinor(0L);
        payment.setCurrency("BRL");
        payment.setDescription("Lifecycle test payment");
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);
    }

    @Test
    void shouldMoveFromPendingToAuthorizedAndThenCaptured() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin.getEmail(),
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                );

        mockMvc.perform(patch("/api/v1/payments/{id}/status", payment.getId())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "AUTHORIZED",
                                  "reason": "Funds reserved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        Payment authorizedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.AUTHORIZED, authorizedPayment.getStatus());

        mockMvc.perform(patch("/api/v1/payments/{id}/status", payment.getId())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CAPTURED",
                                  "reason": "Settlement completed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.status").value("CAPTURED"));

        Payment capturedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.CAPTURED, capturedPayment.getStatus());
    }

    @Test
    void shouldRejectInvalidTransitionFromPendingToCaptured() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin.getEmail(),
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                );

        mockMvc.perform(patch("/api/v1/payments/{id}/status", payment.getId())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CAPTURED",
                                  "reason": "Trying to skip authorization"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid payment status transition from PENDING to CAPTURED"));

        Payment unchangedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.PENDING, unchangedPayment.getStatus());
    }
}