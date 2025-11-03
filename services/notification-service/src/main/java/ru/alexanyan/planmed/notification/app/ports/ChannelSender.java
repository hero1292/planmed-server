package ru.alexanyan.planmed.notification.app.ports;

public interface ChannelSender {
    void sendEmail(String patientId, String subject, String body);
    void sendSms(String patientId, String body);
    void sendPush(String patientId, String title, String body);
}
