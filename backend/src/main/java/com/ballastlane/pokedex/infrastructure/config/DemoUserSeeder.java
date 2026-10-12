package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.port.PasswordHasher;
import com.ballastlane.pokedex.application.port.UserRepository;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Creates demo accounts on startup (idempotent). Disable with app.seed.users=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.users", havingValue = "true", matchIfMissing = true)
public class DemoUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Clock clock;

    public DemoUserSeeder(UserRepository users, PasswordHasher hasher, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("admin", "admin123", Role.ADMIN);
        seed("ash", "pikachu123", Role.USER);
    }

    private void seed(String username, String password, Role role) {
        if (!users.existsByUsername(username)) {
            users.save(new User(null, username, hasher.hash(password), role, clock.instant()));
            log.info("Demo user '{}' ({}) created", username, role);
        }
    }
}
