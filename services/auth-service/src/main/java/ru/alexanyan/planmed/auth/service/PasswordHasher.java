package ru.alexanyan.planmed.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordHasher {

    private final PasswordEncoder encoder;

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
