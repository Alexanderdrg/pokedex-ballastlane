package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.PasswordHasher;
import com.ballastlane.pokedex.application.port.TokenIssuer;
import com.ballastlane.pokedex.application.port.UserRepository;
import com.ballastlane.pokedex.domain.exception.ConflictException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.model.AuthToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.domain.policy.CredentialsPolicy;

import java.time.Clock;
import java.util.Locale;

/**
 * User registration and authentication.
 */
public class AuthUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokens;
    private final CredentialsPolicy policy;
    private final Clock clock;

    public AuthUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokens,
                       CredentialsPolicy policy, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
        this.policy = policy;
        this.clock = clock;
    }

    public AuthToken register(String username, String password) {
        policy.validate(username, password);
        String normalized = username.toLowerCase(Locale.ROOT);
        if (users.existsByUsername(normalized)) {
            throw new ConflictException("Username '" + normalized + "' is already taken");
        }
        User saved = users.save(new User(null, normalized, hasher.hash(password), Role.USER, clock.instant()));
        return tokens.issue(saved);
    }

    public AuthToken login(String username, String password) {
        if (username == null || password == null) {
            throw new InvalidCredentialsException();
        }
        // Same error for unknown user and wrong password: do not leak which usernames exist
        User user = users.findByUsername(username.trim().toLowerCase(Locale.ROOT))
                .filter(u -> hasher.matches(password, u.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return tokens.issue(user);
    }
}
