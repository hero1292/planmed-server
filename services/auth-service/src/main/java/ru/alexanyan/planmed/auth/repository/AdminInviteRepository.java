package ru.alexanyan.planmed.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexanyan.planmed.auth.model.AdminInvite;

import java.util.Optional;
import java.util.UUID;

public interface AdminInviteRepository extends JpaRepository<AdminInvite, UUID> {
    Optional<AdminInvite> findByTokenHash(String tokenHash);
    Optional<AdminInvite> findByEmailIgnoreCaseAndUsedAtIsNull(String email);
}

