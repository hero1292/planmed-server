package ru.alexanyan.planmed.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexanyan.planmed.auth.model.User;
import ru.alexanyan.planmed.auth.model.UserStatus;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);
    long countByStatus(UserStatus status);
}
