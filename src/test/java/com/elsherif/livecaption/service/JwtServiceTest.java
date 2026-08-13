package com.elsherif.livecaption.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yNTYtYml0cw==");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        userDetails = new User("test@example.com", "password", Collections.emptyList());
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void shouldValidateToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateTokenForDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("other@example.com", "password", Collections.emptyList());

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertThat(isValid).isFalse();
    }
}
