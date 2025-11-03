package ru.alexanyan.planmed.notification.service;

import org.springframework.stereotype.Component;

@Component
public class EmailComposer {

    public Message composeVerifyEmail(String to, String verifyUrl) {
        var subject = "Подтвердите e-mail для регистрации";
        var body = """
        Здравствуйте!

        Вы зарегистрировались в системе. Для подтверждения e-mail перейдите по ссылке:
        %s

        Если вы не регистрировались, просто проигнорируйте письмо.
        """.formatted(verifyUrl);
        return new Message(to, subject, body);
    }

    public Message composeAdminInvite(String to, String inviteUrl) {
        var subject = "Приглашение администратора";
        var body = """
        Вас пригласили стать администратором системы.

        Для принятия приглашения перейдите по ссылке:
        %s

        Ссылка имеет ограниченный срок действия.
        """.formatted(inviteUrl);
        return new Message(to, subject, body);
    }

    public record Message(String to, String subject, String body) {}
}

