package com.carlos.payflowguard;

import com.carlos.payflowguard.security.JwtService;
import com.carlos.payflowguard.testsupport.IsolatedSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PayflowguardApplicationTests extends IsolatedSpringBootTest {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private Environment environment;

	@Test
	void contextLoadsWithTestOnlyJwtSigningMaterial() {
		String configuredSecret = environment.getRequiredProperty("app.security.jwt.secret");
		assertTrue(configuredSecret.startsWith("test-only-"));

		String token = jwtService.generateToken("context-test@example.com");
		assertTrue(jwtService.isValid(token));
		assertEquals("context-test@example.com", jwtService.extractEmail(token));
	}

}
