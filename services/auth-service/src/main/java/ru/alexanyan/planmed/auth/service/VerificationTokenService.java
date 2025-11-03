package ru.alexanyan.planmed.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alexanyan.planmed.auth.events.DomainEvents;
import ru.alexanyan.planmed.auth.events.KafkaEventProducer;
import ru.alexanyan.planmed.auth.model.EmailVerificationToken;
import ru.alexanyan.planmed.auth.model.UserStatus;
import ru.alexanyan.planmed.auth.model.VerifyResult;
import ru.alexanyan.planmed.auth.repository.EmailVerificationTokenRepository;
import ru.alexanyan.planmed.auth.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenService {

    private final EmailVerificationTokenRepository tokens;
    private final UserRepository users;
    private final KafkaEventProducer events;

    @Transactional
    public VerifyResult verifyEmailToken(String opaqueToken) {
        String hash = TokenUtil.sha256Hex(opaqueToken);
        Optional<EmailVerificationToken> opt = tokens.findByTokenHash(hash);
        if (opt.isEmpty()) return VerifyResult.INVALID;

        var token = opt.get();

        if (token.getUsedAt() != null) {
            // idempotent
            var user = users.findById(token.getUserId()).orElseThrow();
            if (user.getStatus() == UserStatus.ACTIVE) return VerifyResult.ALREADY_VERIFIED;
            return VerifyResult.INVALID;
        }

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return VerifyResult.EXPIRED;
        }

        var user = users.findById(token.getUserId()).orElseThrow();

        if (user.getStatus() == UserStatus.ACTIVE) {
            token.setUsedAt(OffsetDateTime.now());
            tokens.save(token);
            return VerifyResult.ALREADY_VERIFIED;
        }

        // Activate user
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(OffsetDateTime.now());
        tokens.invalidateAllForUser(user.getId(), OffsetDateTime.now());
        users.save(user);

        // user.email.verified event
        var verifiedEvent = new DomainEvents.UserEmailVerified(
                new DomainEvents.Base(
                        UUID.randomUUID().toString(),
                        "user.email.verified",
                        OffsetDateTime.now(),
                        "1.0",
                        null,
                        null
                ),
                new DomainEvents.UserEmailVerified.Payload(
                        user.getId(),
                        user.getLogin(),
                        user.getLogin()
                )
        );

        events.send("user.email.verified", user.getId().toString(), verifiedEvent);

        // user.activated event
        var activatedEvent = new DomainEvents.UserActivated(
                new DomainEvents.Base(
                        UUID.randomUUID().toString(),
                        "user.activated",
                        OffsetDateTime.now(),
                        "1.0",
                        null,
                        null
                ),
                new DomainEvents.UserActivated.Payload(
                        user.getId(),
                        user.getRoleCode(),
                        user.getLogin(),
                        user.getLogin()
                )
        );

        events.send("user.activated", user.getId().toString(), activatedEvent);

        log.info("audit=verify userId={} login={}", user.getId(), user.getLogin());
        return VerifyResult.VERIFIED;
    }
}
