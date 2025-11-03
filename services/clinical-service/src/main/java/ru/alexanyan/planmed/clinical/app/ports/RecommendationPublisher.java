package ru.alexanyan.planmed.clinical.app.ports;

public interface RecommendationPublisher {
    void publishSaved(String recommendationId);
    void publishUpdated(String recommendationId);
}
