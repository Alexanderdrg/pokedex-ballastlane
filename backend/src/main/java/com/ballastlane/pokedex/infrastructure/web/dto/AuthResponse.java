package com.ballastlane.pokedex.infrastructure.web.dto;

import com.ballastlane.pokedex.domain.model.AuthToken;

import java.time.Instant;

public record AuthResponse(String token, String tokenType, Instant expiresAt, String username, String role) {

    public static AuthResponse from(AuthToken t) {
        return new AuthResponse(t.token(), "Bearer", t.expiresAt(), t.username(), t.role().name());
    }
}
