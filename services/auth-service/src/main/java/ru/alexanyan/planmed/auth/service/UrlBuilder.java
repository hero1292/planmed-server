package ru.alexanyan.planmed.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UrlBuilder {

    @Value("${app.public-base-url}")
    private String base;

    @jakarta.annotation.PostConstruct
    private void normalizeBase() {
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
    }

    public String verifyUrl(String token) {
        return base + "/auth/verify?token=" + token;
    }

    public String adminInviteUrl(String token) {
        return base + "/admin/invites/accept?token=" + token;
    }
}
