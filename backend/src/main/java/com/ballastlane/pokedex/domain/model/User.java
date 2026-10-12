package com.ballastlane.pokedex.domain.model;

import java.time.Instant;

public record User(Long id, String username, String passwordHash, Role role, Instant createdAt) {
}
