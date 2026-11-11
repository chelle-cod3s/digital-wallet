package com.digitalwallet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.digitalwallet.config.JwtProperties;
import com.digitalwallet.user.Role;
import com.digitalwallet.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    @Test
    void tokenRoundTripKeepsTheEmail() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("local-dev-jwt-secret-do-not-commit-9f3c1a7e5b2d48c6");
        properties.setExpirationMs(60_000);

        User user = new User();
        user.setName("Ada");
        user.setEmail("ada@example.com");
        user.setRole(Role.CUSTOMER);

        JwtService jwtService = new JwtService(properties);
        String token = jwtService.generateToken(user);

        assertEquals("ada@example.com", jwtService.extractSubject(token));

        UserDetails details = org.springframework.security.core.userdetails.User.builder()
                .username("ada@example.com")
                .password("ignored")
                .roles("CUSTOMER")
                .build();
        assertTrue(jwtService.isTokenValid(token, details));
    }
}
