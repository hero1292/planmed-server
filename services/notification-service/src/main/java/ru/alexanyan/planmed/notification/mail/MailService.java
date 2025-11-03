package ru.alexanyan.planmed.notification.mail;

public interface MailService {
    void send(String to, String subject, String body);
}
