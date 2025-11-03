package ru.alexanyan.planmed.background.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryJob {

    private final KafkaTemplate<String, Object> kafka;

    @Scheduled(cron = "${jobs.email-retry.cron}")
    public void retry() {
        kafka.send("notification.retry.tick", "tick", Map.of("at", OffsetDateTime.now().toString()));
        log.info("email retry tick emitted");
    }
}
