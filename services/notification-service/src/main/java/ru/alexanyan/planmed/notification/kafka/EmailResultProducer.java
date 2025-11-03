package ru.alexanyan.planmed.notification.kafka;

import ru.alexanyan.planmed.notification.model.Events;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class EmailResultProducer {
    private final KafkaTemplate<String, Object> template;

    public EmailResultProducer(KafkaTemplate<String, Object> template) { this.template = template; }

    public void sent(UUID userId, String email, String reason) {
        var base = new Events.Base(UUID.randomUUID().toString(),"user.email.verification.sent",
                OffsetDateTime.now(),"1.0",null,null);
        var evt = new Events.UserEmailVerificationSent(base, new Events.UserEmailVerificationSent.Payload(userId, email, reason));
        template.send("user.email.verification.sent", userId.toString(), evt);
    }

    public void failed(UUID userId, String email, String reason) {
        var base = new Events.Base(UUID.randomUUID().toString(),"user.email.verification.failed",
                OffsetDateTime.now(),"1.0",null,null);
        var evt = new Events.UserEmailVerificationFailed(base, new Events.UserEmailVerificationFailed.Payload(userId, email, reason));
        template.send("user.email.verification.failed", userId != null? userId.toString() : "unknown", evt);
    }
}

