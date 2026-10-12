package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.application.port.UserRepository;
import com.ballastlane.pokedex.domain.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository jpa;

    public JpaUserRepositoryAdapter(SpringDataUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity saved = jpa.save(new UserEntity(user.id(), user.username(), user.passwordHash(), user.role(),
                user.createdAt()));
        return toDomain(saved);
    }

    private static User toDomain(UserEntity e) {
        return new User(e.getId(), e.getUsername(), e.getPasswordHash(), e.getRole(), e.getCreatedAt());
    }
}
