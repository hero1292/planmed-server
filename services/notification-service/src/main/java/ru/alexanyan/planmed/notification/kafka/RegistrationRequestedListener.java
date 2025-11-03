package ru.alexanyan.planmed.notification.kafka;

import ru.alexanyan.planmed.notification.mail.MailService;
import ru.alexanyan.planmed.notification.service.EmailComposer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RegistrationRequestedListener {
    private static final Logger log = LoggerFactory.getLogger(RegistrationRequestedListener.class);

    private final MailService mail;
    private final EmailComposer composer;
    private final EmailResultProducer resultProducer;

    public RegistrationRequestedListener(MailService mail, EmailComposer composer, EmailResultProducer resultProducer) {
        this.mail = mail; this.composer = composer; this.resultProducer = resultProducer;
    }

    @KafkaListener(topics = "user.registration.requested")
    public void onRegistrationRequested(ConsumerRecord<String, Object> rec) {
        try {
            var payload = (java.util.Map<?,?>)((java.util.Map<?,?>)rec.value()).get("payload");
            var email = (String) payload.get("email");
            var verifyUrl = (String) payload.get("verifyUrl");
            var userId = java.util.UUID.fromString((String) payload.get("userId"));

            var msg = composer.composeVerifyEmail(email, verifyUrl);
            mail.send(email, msg.subject(), msg.body());
            resultProducer.sent(userId, email, "ok");
            log.info("sent verify email to {}", email);
        } catch (Exception e) {
            log.warn("failed to send verify email: {}", e.getMessage());
            // best effort: emit failure with partial info
            resultProducer.failed(null, null, e.getMessage());
        }
    }
}
