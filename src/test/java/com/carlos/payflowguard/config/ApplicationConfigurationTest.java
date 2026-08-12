package com.carlos.payflowguard.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationConfigurationTest {

    private final PropertySource<?> applicationProperties = loadApplicationProperties();

    @Test
    void productionWebhookUrlUsesEnvironmentOverrideWithLoopbackDefault() {
        assertEquals(
                "${PAYFLOW_WEBHOOK_URL:http://127.0.0.1:9999/webhook}",
                applicationProperties.getProperty("app.webhooks.payment-status-url")
        );
    }

    @Test
    void productionJwtSecretHasNoRepositoryDefault() {
        assertEquals(
                "${JWT_SECRET}",
                applicationProperties.getProperty("app.security.jwt.secret")
        );
    }

    private PropertySource<?> loadApplicationProperties() {
        try {
            return new YamlPropertySourceLoader()
                    .load("application", new ClassPathResource("application.yaml"))
                    .getFirst();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load application.yaml", exception);
        }
    }
}
