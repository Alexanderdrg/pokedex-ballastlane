package com.ballastlane.pokedex.infrastructure.security;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";
    private final JwtProperties props = new JwtProperties(SECRET, Duration.ofMinutes(30), "pokedex-api");
    private final Clock clock = Clock.fixed(TestData.NOW, ZoneOffset.UTC);
    private final JwtTokenService service = new JwtTokenService(props, clock);
    private final User admin = new User(1L, "admin", "hash", Role.ADMIN, TestData.NOW);

    @Test
    void issuedTokenCanBeVerified() {
        var token = service.issue(admin);

        assertThat(token.expiresAt()).isEqualTo(TestData.NOW.plus(Duration.ofMinutes(30)));
        assertThat(service.verify(token.token()))
                .contains(new AuthenticatedUser("admin", Role.ADMIN));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.issue(admin).token();
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThat(service.verify(tampered)).isEmpty();
        assertThat(service.verify("not-a-jwt")).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        String token = service.issue(admin).token();
        var later = new JwtTokenService(props, Clock.fixed(TestData.NOW.plus(Duration.ofHours(1)), ZoneOffset.UTC));

        assertThat(later.verify(token)).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        var other = new JwtTokenService(
                new JwtProperties("another-secret-another-secret-another-1", Duration.ofMinutes(30), "pokedex-api"),
                clock);

        assertThat(service.verify(other.issue(admin).token())).isEmpty();
    }

    @Test
    void shortSecretsAreRefusedAtStartup() {
        assertThatThrownBy(() -> new JwtProperties("short", null, null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void optionalSettingsHaveSafeDefaults() {
        var defaults = new JwtProperties(SECRET, null, " ");

        assertThat(defaults.expiration()).isEqualTo(Duration.ofHours(2));
        assertThat(defaults.issuer()).isEqualTo("pokedex-api");
        assertThatThrownBy(() -> new JwtProperties(null, null, null)).isInstanceOf(IllegalStateException.class);
    }
}
