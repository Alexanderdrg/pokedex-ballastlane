package com.ballastlane.pokedex.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, Duration expiration, String issuer) {

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("security.jwt.secret must be at least 32 bytes long (HS256)");
        }
        if (expiration == null) {
            expiration = Duration.ofHours(2);
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "pokedex-api";
        }
    }
}
