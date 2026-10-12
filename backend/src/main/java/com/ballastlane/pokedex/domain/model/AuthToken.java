package com.ballastlane.pokedex.domain.model;

import java.time.Instant;

public record AuthToken(String token, Instant expiresAt, String username, Role role) {
}
