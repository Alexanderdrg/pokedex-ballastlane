package com.ballastlane.pokedex.application;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.application.port.PasswordHasher;
import com.ballastlane.pokedex.application.port.TokenIssuer;
import com.ballastlane.pokedex.application.port.UserRepository;
import com.ballastlane.pokedex.application.usecase.AuthUseCase;
import com.ballastlane.pokedex.domain.exception.ConflictException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.AuthToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.domain.policy.CredentialsPolicy;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthUseCaseTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final TokenIssuer tokens = mock(TokenIssuer.class);
    private final AuthUseCase useCase = new AuthUseCase(users, hasher, tokens, new CredentialsPolicy(),
            Clock.fixed(TestData.NOW, ZoneOffset.UTC));

    private final User misty = new User(1L, "misty", "hashed", Role.USER, TestData.NOW);
    private final AuthToken token = new AuthToken("jwt", TestData.NOW.plusSeconds(60), "misty", Role.USER);

    @Test
    void registerHashesPasswordAndAssignsUserRole() {
        when(hasher.hash("starmie123")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tokens.issue(any())).thenReturn(token);

        AuthToken result = useCase.register("Misty", "starmie123");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().username()).isEqualTo("misty");
        assertThat(saved.getValue().passwordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().role()).isEqualTo(Role.USER);
        assertThat(result).isEqualTo(token);
    }

    @Test
    void registerRejectsDuplicatedUsername() {
        when(users.existsByUsername("misty")).thenReturn(true);

        assertThatThrownBy(() -> useCase.register("misty", "starmie123")).isInstanceOf(ConflictException.class);
        verify(users, never()).save(any());
    }

    @Test
    void registerRejectsWeakPassword() {
        assertThatThrownBy(() -> useCase.register("misty", "weak")).isInstanceOf(ValidationException.class);
    }

    @Test
    void loginIssuesTokenForValidCredentials() {
        when(users.findByUsername("misty")).thenReturn(Optional.of(misty));
        when(hasher.matches("starmie123", "hashed")).thenReturn(true);
        when(tokens.issue(misty)).thenReturn(token);

        assertThat(useCase.login(" MISTY ", "starmie123")).isEqualTo(token);
    }

    @Test
    void loginFailsWithSameErrorForWrongPasswordAndUnknownUser() {
        when(users.findByUsername("misty")).thenReturn(Optional.of(misty));
        when(hasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> useCase.login("misty", "wrong")).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> useCase.login("ghost", "whatever")).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> useCase.login(null, null)).isInstanceOf(InvalidCredentialsException.class);
    }
}
