package com.carlos.payflowguard.payment;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.entity.Refund;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.payment.repository.RefundRepository;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RefundHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private Merchant merchant;
    private Payment payment;

    @BeforeEach
    void setup() {
        refundRepository.deleteAll();
        paymentRepository.deleteAll();
        merchantRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword("123");
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        merchant = new Merchant();
        merchant.setBusinessName("Test Merchant");
        merchant.setEmail("merchant@test.com");
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setUser(admin);
        merchant = merchantRepository.save(merchant);

        payment = new Payment();
        payment.setMerchant(merchant);
        payment.setAmountMinor(1000L);
        payment.setRefundedAmountMinor(1000L);
        payment.setCurrency("BRL");
        payment.setDescription("Refund history test");
        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        Refund r1 = new Refund();
        r1.setPayment(payment);
        r1.setAmountMinor(200L);
        r1.setReason("First refund");

        Refund r2 = new Refund();
        r2.setPayment(payment);
        r2.setAmountMinor(300L);
        r2.setReason("Second refund");

        Refund r3 = new Refund();
        r3.setPayment(payment);
        r3.setAmountMinor(500L);
        r3.setReason("Final refund");

        refundRepository.saveAll(List.of(r1, r2, r3));
    }

    @Test
    void shouldReturnRefundHistoryInOrder() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin.getEmail(),
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                );

        mockMvc.perform(get("/api/v1/payments/{id}/refunds", payment.getId())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))

                .andExpect(jsonPath("$[0].paymentId").value(payment.getId()))
                .andExpect(jsonPath("$[0].amountMinor").value(200))
                .andExpect(jsonPath("$[0].reason").value("First refund"))

                .andExpect(jsonPath("$[1].paymentId").value(payment.getId()))
                .andExpect(jsonPath("$[1].amountMinor").value(300))
                .andExpect(jsonPath("$[1].reason").value("Second refund"))

                .andExpect(jsonPath("$[2].paymentId").value(payment.getId()))
                .andExpect(jsonPath("$[2].amountMinor").value(500))
                .andExpect(jsonPath("$[2].reason").value("Final refund"));
    }
}