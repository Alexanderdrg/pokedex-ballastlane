package com.ballastlane.pokedex.infrastructure.security;

import com.ballastlane.pokedex.application.port.TokenIssuer;
import com.ballastlane.pokedex.domain.model.AuthToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and verifies stateless HS256 JWTs. The role travels as a claim so
 * authorization does not need a database round-trip per request.
 */
public class JwtTokenService implements TokenIssuer {

    private static final String ROLE_CLAIM = "role";

    private final JwtProperties properties;
    private final SecretKey key;
    private final Clock clock;

    public JwtTokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
    }

    @Override
    public AuthToken issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.expiration());
        String token = Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.username())
                .claim(ROLE_CLAIM, user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new AuthToken(token, expiresAt, user.username(), user.role());
    }

    public Optional<AuthenticatedUser> verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.issuer())
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Role role = Role.valueOf(claims.get(ROLE_CLAIM, String.class));
            return Optional.of(new AuthenticatedUser(claims.getSubject(), role));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
