package com.carlos.payflowguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI payflowGuardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayFlow Guard API")
                        .description("Payment orchestration and merchant management API built with Spring Boot.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Carlos Eduardo Freire de Souza")
                                .url("https://github.com/souzacef/payflow-guard")));
    }
}