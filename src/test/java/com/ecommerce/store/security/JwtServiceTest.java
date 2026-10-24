package com.ecommerce.store.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test void issuesAndValidatesSignedTokens() {
        JwtService service = new JwtService("this-is-a-test-secret-that-is-long-enough-123456", 60_000);
        var user = User.withUsername("shopper@example.com").password("encoded").roles("CUSTOMER").build();
        String token = service.issue(user);
        assertEquals("shopper@example.com", service.subject(token));
        assertTrue(service.valid(token, user));
        assertFalse(service.valid(token, User.withUsername("other@example.com").password("x").roles("CUSTOMER").build()));
    }
}
