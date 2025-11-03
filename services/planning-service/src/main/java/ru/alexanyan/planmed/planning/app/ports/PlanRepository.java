package ru.alexanyan.planmed.planning.app.ports;

public interface PlanRepository {
    String savePlan(String patientId, String sourceRecommendationId);
    void upsertEvent(String planId, String itemId, String scheduledAt, String windowStart, String windowEnd, String status);
}
