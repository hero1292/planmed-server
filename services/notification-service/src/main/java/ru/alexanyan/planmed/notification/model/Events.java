package ru.alexanyan.planmed.notification.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class Events {
    public record Base(String eventId, String type, OffsetDateTime occurredAt,
                       String schemaVersion, String correlationId, String causationId) {}

    public record UserEmailVerificationSent(Base base, Payload payload) {
        public record Payload(UUID userId, String email, String reason) {}
    }
    public record UserEmailVerificationFailed(Base base, Payload payload) {
        public record Payload(UUID userId, String email, String reason) {}
    }
}

