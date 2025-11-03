package ru.alexanyan.planmed.adherence.app.ports;

import java.time.Instant;
import java.util.Optional;

public interface AdherenceRepository {
    Optional<String> findRecordIdByEventId(String planEventId);
    String upsert(String planEventId, String patientId, String status, Instant actualTakenAt, String comment);
}
