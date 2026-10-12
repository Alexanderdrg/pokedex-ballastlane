package com.ballastlane.pokedex.domain;

import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.policy.CredentialsPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredentialsPolicyTest {

    private final CredentialsPolicy policy = new CredentialsPolicy();

    @Test
    void acceptsValidCredentials() {
        assertThatCode(() -> policy.validate("misty_99", "starmie123")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "ab, password123",          // username too short
            "bad user, password123",    // space in username
            "brock, short1",            // password too short
            "brock, onlyletters",       // no digit
            "brock, 12345678"           // no letter
    })
    void rejectsInvalidCredentials(String username, String password) {
        assertThatThrownBy(() -> policy.validate(username, password)).isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsNulls() {
        assertThatThrownBy(() -> policy.validate(null, null)).isInstanceOf(ValidationException.class);
    }
}
