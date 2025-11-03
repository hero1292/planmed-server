package ru.alexanyan.planmed.auth.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class DomainEvents {

    public record Base(String eventId, String type, OffsetDateTime occurredAt,
                       String schemaVersion, String correlationId, String causationId) {}

    public record UserRegistrationRequested(Base base, Payload payload) {
        public record Payload(UUID userId, String login, String email, String verifyUrl) {}
    }

    public record UserEmailVerificationSent(Base base, Payload payload) {
        public record Payload(UUID userId, String email, String reason) {}
    }

    public record UserEmailVerificationFailed(Base base, Payload payload) {
        public record Payload(UUID userId, String email, String reason) {}
    }

    public record UserEmailVerified(Base base, Payload payload) {
        public record Payload(UUID userId, String login, String email) {}
    }

    public record UserActivated(Base base, Payload payload) {
        public record Payload(UUID userId, String role, String login, String email) {}
    }

    public record AdminInviteCreated(Base base, Payload payload) {
        public record Payload(UUID inviteId, String email, String inviteUrl, OffsetDateTime expiresAt) {}
    }

    public record AdminInviteAccepted(Base base, Payload payload) {
        public record Payload(UUID inviteId, UUID adminUserId, String email) {}
    }

    public record AdminInviteExpired(Base base, Payload payload) {
        public record Payload(UUID inviteId, String email) {}
    }

    private DomainEvents() {}
}
