package ru.alexanyan.planmed.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alexanyan.planmed.auth.events.DomainEvents;
import ru.alexanyan.planmed.auth.events.KafkaEventProducer;
import ru.alexanyan.planmed.auth.model.EmailVerificationToken;
import ru.alexanyan.planmed.auth.model.User;
import ru.alexanyan.planmed.auth.model.UserStatus;
import ru.alexanyan.planmed.auth.repository.EmailVerificationTokenRepository;
import ru.alexanyan.planmed.auth.repository.UserRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final PasswordHasher hasher;
    private final UrlBuilder urlBuilder;
    private final KafkaEventProducer events;

    @Value("${app.verification.ttl-hours}")
    private long ttlHours;

    @Value("${app.verification.resend-min-interval}")
    private Duration resendInterval;

    @Transactional
    public void register(String login, String password, String role, String email) {
        if (users.existsByLogin(login)) {
            throw new IllegalArgumentException("login already exists");
        }

        var user = new User();
        user.setLogin(login);
        user.setPasswordHash(hasher.hash(password));
        user.setRoleCode(role);
        user.setStatus(UserStatus.PENDING);
        users.save(user);

        issueTokenAndPublish(user.getId(), user.getLogin(), email, true);
        log.info("audit=register userId={} login={} role={}", user.getId(), login, role);
    }

    @Transactional
    public void resendVerification(String loginOrEmail) {
        var user = users.findByLogin(loginOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("already active");
        }

        var actives = tokens.findActiveByUser(user.getId());
        if (!actives.isEmpty()) {
            var newest = actives.get(0);
            var minutesSinceLast = Duration
                    .between(newest.getExpiresAt().minusHours(ttlHours), OffsetDateTime.now())
                    .abs()
                    .toMinutes();

            if (minutesSinceLast < resendInterval.toMinutes()) {
                throw new TooFrequentException();
            }
        }

        issueTokenAndPublish(user.getId(), user.getLogin(), loginOrEmail, false);
        log.info("audit=resend userId={} login={}", user.getId(), user.getLogin());
    }

    private void issueTokenAndPublish(UUID userId, String login, String email, boolean invalidateOld) {
        if (invalidateOld) {
            tokens.invalidateAllForUser(userId, OffsetDateTime.now());
        }

        var opaque = TokenUtil.newOpaqueToken();
        var token = new EmailVerificationToken();
        token.setUserId(userId);
        token.setTokenHash(TokenUtil.sha256Hex(opaque));
        token.setExpiresAt(OffsetDateTime.now().plusHours(ttlHours));
        tokens.save(token);

        var verifyUrl = urlBuilder.verifyUrl(opaque);
        var base = new DomainEvents.Base(
                UUID.randomUUID().toString(),
                "user.registration.requested",
                OffsetDateTime.now(),
                "1.0",
                null,
                null
        );

        var event = new DomainEvents.UserRegistrationRequested(
                base,
                new DomainEvents.UserRegistrationRequested.Payload(userId, login, email, verifyUrl)
        );

        events.send("user.registration.requested", userId.toString(), event);
    }

    public static class TooFrequentException extends RuntimeException {}
}
