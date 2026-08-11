package com.carlos.payflowguard.payment.fraud;

import com.carlos.payflowguard.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class FraudRuleSpringOrderingTest {

    @Test
    void shouldDiscoverProductionFraudRulesInConfiguredOrder() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(PaymentRepository.class, () -> mock(PaymentRepository.class));
            context.registerBean(RuleOrderProbe.class);
            context.scan(FraudRule.class.getPackageName());
            context.refresh();

            RuleOrderProbe probe = context.getBean(RuleOrderProbe.class);

            assertEquals(
                    List.of(AmountThresholdFraudRule.class, VelocityFraudRule.class),
                    probe.rules().stream().map(Object::getClass).toList()
            );
        }
    }

    record RuleOrderProbe(List<FraudRule> rules) {
    }
}
