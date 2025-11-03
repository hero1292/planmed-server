package ru.alexanyan.planmed.profile.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
@Slf4j
public class PiiCrypto {

    private final byte[] key;
    private static final SecureRandom RNG = new SecureRandom();

    public PiiCrypto(@Value("${pii.encryption.secret}") String secret) {
        this.key = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 32);
    }

    public byte[] encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[12];
            RNG.nextBytes(iv);

            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv)
            );

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // prepend IV to ciphertext
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

            return result;
        } catch (Exception e) {
            log.error("PII encryption failed", e);
            throw new IllegalStateException("PII encryption error", e);
        }
    }
}
