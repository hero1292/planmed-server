package ru.alexanyan.planmed.security.mfa;

public interface MfaService {
    String initTotpSecret(String userId);
    boolean verifyTotp(String userId, String code);
}
