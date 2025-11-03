package ru.alexanyan.planmed.security.jwt;

import java.util.Map;

public interface TokenService {
    String issueAccessToken(String subject, Map<String, Object> claims);
    String issueRefreshToken(String subject, Map<String, Object> claims);
    boolean validate(String token);
}
