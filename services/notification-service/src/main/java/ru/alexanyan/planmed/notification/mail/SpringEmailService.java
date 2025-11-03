package ru.alexanyan.planmed.notification.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SpringEmailService implements MailService {
    private static final Logger log = LoggerFactory.getLogger(SpringEmailService.class);
    private final JavaMailSender sender;

    public SpringEmailService(JavaMailSender sender) { this.sender = sender; }

    @Override
    public void send(String to, String subject, String body) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        sender.send(msg);
        log.debug("email dispatched to {}", to);
    }
}

