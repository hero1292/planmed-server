package ru.alexanyan.planmed.auth.app.ports;

import java.time.Instant;

public interface SessionRepository {
    void create(String userId, Instant expiresAt, String refreshTokenHash, String ip, String userAgent);
    void revokeAllExcept(String userId, String sessionIdToKeep);
}
