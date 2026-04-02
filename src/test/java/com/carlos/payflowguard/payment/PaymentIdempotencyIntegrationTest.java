package com.carlos.payflowguard.payment;

import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.entity.Payment;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentIdempotencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        merchantRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);

        merchant = new Merchant();
        merchant.setBusinessName("Test Merchant");
        merchant.setEmail("merchant@example.com");
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setUser(user);
        merchant = merchantRepository.save(merchant);
    }

    @Test
    void shouldReturnSamePaymentWhenUsingSameIdempotencyKey() throws Exception {
        String requestBody = """
                {
                  "merchantId": %d,
                  "amountMinor": 777,
                  "currency": "BRL",
                  "description": "Idempotency test"
                }
                """.formatted(merchant.getId());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                );

        mockMvc.perform(post("/api/v1/payments")
                        .with(authentication(auth))
                        .header("Idempotency-Key", "abc-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountMinor", is(777)))
                .andExpect(jsonPath("$.description", is("Idempotency test")));

        mockMvc.perform(post("/api/v1/payments")
                        .with(authentication(auth))
                        .header("Idempotency-Key", "abc-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountMinor", is(777)))
                .andExpect(jsonPath("$.description", is("Idempotency test")));

        List<Payment> payments = paymentRepository.findAll();
        assertEquals(1, payments.size());
        assertEquals("abc-123", payments.get(0).getIdempotencyKey());
        assertEquals(777L, payments.get(0).getAmountMinor());
    }
}