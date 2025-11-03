package ru.alexanyan.planmed.profile.kafka;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.alexanyan.planmed.profile.crypto.PiiCrypto;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivatedListener {

    private final EntityManager em;
    private final PiiCrypto crypto;

    @KafkaListener(topics = "user.activated")
    @Transactional
    public void onActivated(ConsumerRecord<String, Object> record) {
        try {
            var value = (Map<?, ?>) record.value();
            var payload = (Map<?, ?>) value.get("payload");

            UUID userId = UUID.fromString((String) payload.get("userId"));
            String role = (String) payload.get("role");

            switch (role) {
                case "PATIENT" -> em.createNativeQuery("""
                        INSERT INTO profile.patient_profiles(user_id, created_at, updated_at)
                        VALUES(?1, now(), now())
                        ON CONFLICT (user_id) DO NOTHING
                        """).setParameter(1, userId)
                        .executeUpdate();

                case "DOCTOR" -> em.createNativeQuery("""
                        INSERT INTO profile.doctor_profiles(user_id, created_at, updated_at)
                        VALUES(?1, now(), now())
                        ON CONFLICT (user_id) DO NOTHING
                        """).setParameter(1, userId)
                        .executeUpdate();

                default -> log.warn("Unknown role '{}' for userId={}", role, userId);
            }

            log.info("Processed user.activated event for userId={} role={}", userId, role);
        } catch (Exception e) {
            log.error("Error processing user.activated event", e);
            throw e;
        }
    }
}
