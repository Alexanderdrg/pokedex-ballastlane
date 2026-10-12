package com.ballastlane.pokedex.application.port;

import com.ballastlane.pokedex.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    User save(User user);
}
