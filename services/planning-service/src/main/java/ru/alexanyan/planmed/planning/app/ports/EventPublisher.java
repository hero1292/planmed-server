package ru.alexanyan.planmed.planning.app.ports;
public interface EventPublisher {
    void publishPlanGenerated(String planId, String patientId);
    void publishPlanEventsUpdated(String planId, String patientId);
}
