package com.carlos.payflowguard.testsupport;

import com.carlos.payflowguard.webhook.service.WebhookEventService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public abstract class IsolatedSpringBootTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    protected WebhookEventService webhookEventService;

    @BeforeEach
    protected final void verifyTestIsolation() throws SQLException {
        boolean testProfileActive = Arrays.asList(environment.getActiveProfiles()).contains("test");
        assertTrue(
                testProfileActive,
                "Refusing to run integration-test setup without the test profile"
        );

        try (Connection connection = dataSource.getConnection()) {
            String actualUrl = connection.getMetaData().getURL();
            assertTrue(
                    actualUrl.startsWith("jdbc:h2:mem:"),
                    () -> "Refusing to run integration-test setup against non-memory datasource: " + actualUrl
            );
            assertFalse(
                    actualUrl.startsWith("jdbc:postgresql:"),
                    () -> "Refusing to run integration-test setup against PostgreSQL datasource: " + actualUrl
            );
        }

        String webhookUrl = environment.getRequiredProperty("app.webhooks.payment-status-url");
        URI webhookUri = URI.create(webhookUrl);
        String webhookScheme = webhookUri.getScheme();
        assertFalse(
                "http".equalsIgnoreCase(webhookScheme) || "https".equalsIgnoreCase(webhookScheme),
                () -> "Refusing to run integration tests with network webhook URL: " + webhookUrl
        );

        Boolean schedulingEnabled = environment.getProperty("app.scheduling.enabled", Boolean.class);
        assertFalse(
                Boolean.TRUE.equals(schedulingEnabled),
                "Refusing to run integration tests while scheduling is enabled"
        );
        assertFalse(
                applicationContext.containsBean("schedulingConfig"),
                "Scheduling configuration must not be active in integration tests"
        );
    }
}
