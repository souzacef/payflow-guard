package com.carlos.payflowguard.payment;

import com.carlos.payflowguard.audit.repository.AuditLogRepository;
import com.carlos.payflowguard.audit.service.AuditLogService;
import com.carlos.payflowguard.merchant.entity.Merchant;
import com.carlos.payflowguard.merchant.entity.MerchantStatus;
import com.carlos.payflowguard.merchant.repository.MerchantRepository;
import com.carlos.payflowguard.payment.dto.UpdatePaymentStatusRequest;
import com.carlos.payflowguard.payment.entity.Payment;
import com.carlos.payflowguard.payment.entity.PaymentStatus;
import com.carlos.payflowguard.payment.repository.PaymentRepository;
import com.carlos.payflowguard.payment.repository.RefundRepository;
import com.carlos.payflowguard.payment.service.PaymentService;
import com.carlos.payflowguard.user.entity.Role;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import com.carlos.payflowguard.webhook.entity.WebhookEvent;
import com.carlos.payflowguard.webhook.entity.WebhookEventStatus;
import com.carlos.payflowguard.webhook.repository.WebhookEventRepository;
import com.carlos.payflowguard.webhook.service.WebhookEventService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class PaymentObserverTransactionIntegrationTest {

    private static final AtomicInteger HTTP_REQUESTS = new AtomicInteger();
    private static final HttpServer HTTP_SERVER = startHttpServer();

    @DynamicPropertySource
    static void webhookUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "app.webhooks.payment-status-url",
                () -> "http://127.0.0.1:" + HTTP_SERVER.getAddress().getPort() + "/payment-status"
        );
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private WebhookEventRepository webhookEventRepository;

    @MockitoSpyBean
    private WebhookEventService webhookEventService;

    @MockitoSpyBean
    private AuditLogService auditLogService;

    private User admin;
    private Payment payment;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        verifyTestIsolation();

        HTTP_REQUESTS.set(0);

        webhookEventRepository.deleteAll();
        auditLogRepository.deleteAll();
        refundRepository.deleteAll();
        paymentRepository.deleteAll();
        merchantRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setEmail("observer-admin@test.com");
        admin.setPassword("123");
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        Merchant merchant = new Merchant();
        merchant.setBusinessName("Observer Merchant");
        merchant.setEmail("observer-merchant@test.com");
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setUser(admin);
        merchant = merchantRepository.save(merchant);

        payment = new Payment();
        payment.setMerchant(merchant);
        payment.setAmountMinor(1500L);
        payment.setRefundedAmountMinor(0L);
        payment.setCurrency("BRL");
        payment.setDescription("Observer transaction test");
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), null)
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterAll
    static void stopHttpServer() {
        HTTP_SERVER.stop(0);
    }

    @Test
    void beforeCommitFailureRollsBackPaymentAuditAndWebhookWithoutHttpDelivery() {
        doThrow(new IllegalStateException("forced audit failure"))
                .when(auditLogService).log(anyString(), anyString(), anyLong(), anyString(), anyString());

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.updatePaymentStatus(
                        payment.getId(),
                        statusUpdate(PaymentStatus.AUTHORIZED, "Funds reserved")
                )
        );

        Payment unchangedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.PENDING, unchangedPayment.getStatus());
        assertEquals(0, auditLogRepository.count());
        assertEquals(0, webhookEventRepository.count());
        assertEquals(0, HTTP_REQUESTS.get());
        verify(webhookEventService, never()).deliverEvent(anyLong());
    }

    @Test
    void deliveryRunsAfterCommitAndPersistsFailedResultIndependently() {
        paymentService.updatePaymentStatus(
                payment.getId(),
                statusUpdate(PaymentStatus.AUTHORIZED, "Funds reserved")
        );

        Payment committedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertEquals(PaymentStatus.AUTHORIZED, committedPayment.getStatus());
        assertEquals(1, auditLogRepository.count());
        assertEquals(1, webhookEventRepository.count());
        assertEquals(1, HTTP_REQUESTS.get());

        WebhookEvent webhookEvent = webhookEventRepository.findAll().getFirst();
        assertEquals(WebhookEventStatus.FAILED, webhookEvent.getStatus());
        assertEquals(1, webhookEvent.getAttemptCount());
        assertEquals(503, webhookEvent.getResponseStatusCode());
        assertEquals("HTTP 503: unavailable", webhookEvent.getLastError());
        verify(webhookEventService).deliverEvent(webhookEvent.getId());
    }

    private UpdatePaymentStatusRequest statusUpdate(PaymentStatus status, String reason) {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(status);
        request.setReason(reason);
        return request;
    }

    private void verifyTestIsolation() throws SQLException, IOException {
        assertTrue(
                Arrays.asList(environment.getActiveProfiles()).contains("test"),
                "Refusing destructive observer-test setup without the test profile"
        );

        try (Connection connection = dataSource.getConnection()) {
            String actualUrl = connection.getMetaData().getURL();
            assertTrue(
                    actualUrl.startsWith("jdbc:h2:mem:"),
                    () -> "Refusing destructive observer-test setup against non-memory datasource: " + actualUrl
            );
            assertFalse(
                    actualUrl.startsWith("jdbc:postgresql:"),
                    () -> "Refusing destructive observer-test setup against PostgreSQL datasource: " + actualUrl
            );
        }

        Boolean schedulingEnabled = environment.getProperty("app.scheduling.enabled", Boolean.class);
        assertEquals(
                Boolean.FALSE,
                schedulingEnabled,
                "Refusing destructive observer-test setup unless app.scheduling.enabled=false"
        );
        assertFalse(
                applicationContext.containsBean("schedulingConfig"),
                "Refusing destructive observer-test setup while scheduling is active"
        );

        String webhookUrl = environment.getRequiredProperty("app.webhooks.payment-status-url");
        URI webhookUri = URI.create(webhookUrl);
        assertTrue(
                "http".equalsIgnoreCase(webhookUri.getScheme())
                        || "https".equalsIgnoreCase(webhookUri.getScheme()),
                () -> "Observer transaction test requires a loopback HTTP webhook URL: " + webhookUrl
        );

        String webhookHost = webhookUri.getHost();
        assertNotNull(webhookHost, "Observer transaction test webhook URL must have a host");
        InetAddress[] webhookAddresses = InetAddress.getAllByName(webhookHost);
        assertTrue(
                webhookAddresses.length > 0
                        && Arrays.stream(webhookAddresses).allMatch(InetAddress::isLoopbackAddress),
                () -> "Refusing observer transaction test with non-loopback webhook URL: " + webhookUrl
        );
        assertEquals(
                HTTP_SERVER.getAddress().getPort(),
                webhookUri.getPort(),
                "Observer transaction test webhook must target its local test server"
        );
    }

    private static HttpServer startHttpServer() {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), 0),
                    0
            );
            server.createContext("/payment-status", exchange -> {
                HTTP_REQUESTS.incrementAndGet();
                byte[] body = "unavailable".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(503, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

}
