package ru.alexanyan.planmed.notification.kafka;

import ru.alexanyan.planmed.notification.mail.MailService;
import ru.alexanyan.planmed.notification.service.EmailComposer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AdminInviteCreatedListener {
    private final MailService mail;
    private final EmailComposer composer;

    public AdminInviteCreatedListener(MailService mail, EmailComposer composer) {
        this.mail = mail; this.composer = composer;
    }

    @KafkaListener(topics = "admin.invite.created")
    public void onInvite(ConsumerRecord<String, Object> rec) {
        var payload = (java.util.Map<?,?>)((java.util.Map<?,?>)rec.value()).get("payload");
        var email = (String) payload.get("email");
        var inviteUrl = (String) payload.get("inviteUrl");
        var msg = composer.composeAdminInvite(email, inviteUrl);
        mail.send(email, msg.subject(), msg.body());
    }
}

