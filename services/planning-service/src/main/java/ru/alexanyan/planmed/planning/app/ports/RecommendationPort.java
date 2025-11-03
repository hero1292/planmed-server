package ru.alexanyan.planmed.planning.app.ports;

public interface RecommendationPort {
    RecommendationSnapshot loadSnapshot(String recommendationId);
    record RecommendationSnapshot(String id, String patientId, String startDateJson, String itemsJson) {}
}
