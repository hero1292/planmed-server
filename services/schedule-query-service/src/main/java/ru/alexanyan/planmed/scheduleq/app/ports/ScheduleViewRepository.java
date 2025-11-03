package ru.alexanyan.planmed.scheduleq.app.ports;

import java.time.Instant;
import java.util.List;

public interface ScheduleViewRepository {
    List<EventView> findUpcoming(String patientId, Instant from, Instant to);
    record EventView(String id, String status, Instant scheduledAt, Instant windowStart, Instant windowEnd) {}
}
