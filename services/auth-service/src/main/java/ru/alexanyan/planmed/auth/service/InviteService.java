package ru.alexanyan.planmed.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alexanyan.planmed.auth.events.DomainEvents;
import ru.alexanyan.planmed.auth.events.KafkaEventProducer;
import ru.alexanyan.planmed.auth.model.AdminInvite;
import ru.alexanyan.planmed.auth.model.InviteAcceptResult;
import ru.alexanyan.planmed.auth.model.User;
import ru.alexanyan.planmed.auth.model.UserStatus;
import ru.alexanyan.planmed.auth.repository.AdminInviteRepository;
import ru.alexanyan.planmed.auth.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private final AdminInviteRepository invites;
    private final UserRepository users;
    private final UrlBuilder urlBuilder;
    private final KafkaEventProducer events;

    @Value("${app.invite.ttl-hours}")
    private long ttlHours;

    @Transactional
    public void createInvite(String email) {
        invites.findByEmailIgnoreCaseAndUsedAtIsNull(email)
                .ifPresent(i -> { throw new IllegalStateException("invite exists"); });

        var opaque = TokenUtil.newOpaqueToken();
        var inv = new AdminInvite();
        inv.setEmail(email);
        inv.setTokenHash(TokenUtil.sha256Hex(opaque));
        inv.setExpiresAt(OffsetDateTime.now().plusHours(ttlHours));
        invites.save(inv);

        var base = new DomainEvents.Base(
                UUID.randomUUID().toString(),
                "admin.invite.created",
                OffsetDateTime.now(),
                "1.0",
                null,
                null
        );

        var evt = new DomainEvents.AdminInviteCreated(
                base,
                new DomainEvents.AdminInviteCreated.Payload(
                        inv.getId(),
                        email,
                        urlBuilder.adminInviteUrl(opaque),
                        inv.getExpiresAt()
                )
        );

        events.send("admin.invite.created", inv.getId().toString(), evt);
        log.info("audit=invite.create inviteId={} email={}", inv.getId(), email);
    }

    @Transactional
    public InviteAcceptResult acceptInvite(String opaqueToken) {
        var hash = TokenUtil.sha256Hex(opaqueToken);
        var inv = invites.findByTokenHash(hash).orElse(null);

        if (inv == null) return InviteAcceptResult.INVALID;
        if (inv.getUsedAt() != null) return InviteAcceptResult.ALREADY_ACCEPTED;

        if (inv.getExpiresAt().isBefore(OffsetDateTime.now())) {
            var baseExp = new DomainEvents.Base(
                    UUID.randomUUID().toString(),
                    "admin.invite.expired",
                    OffsetDateTime.now(),
                    "1.0",
                    null,
                    null
            );

            events.send(
                    "admin.invite.expired",
                    inv.getId().toString(),
                    new DomainEvents.AdminInviteExpired(
                            baseExp,
                            new DomainEvents.AdminInviteExpired.Payload(inv.getId(), inv.getEmail())
                    )
            );

            return InviteAcceptResult.EXPIRED;
        }

        // Create or activate admin user
        var user = users.findByLogin(inv.getEmail()).orElseGet(() -> {
            var u = new User();
            u.setLogin(inv.getEmail());
            u.setPasswordHash("{noop}"); // password to be set later via reset flow
            u.setRoleCode("ADMIN");
            u.setStatus(UserStatus.ACTIVE);
            return users.save(u);
        });

        inv.setUsedAt(OffsetDateTime.now());
        invites.save(inv);

        var baseAcc = new DomainEvents.Base(
                UUID.randomUUID().toString(),
                "admin.invite.accepted",
                OffsetDateTime.now(),
                "1.0",
                null,
                null
        );

        events.send(
                "admin.invite.accepted",
                inv.getId().toString(),
                new DomainEvents.AdminInviteAccepted(
                        baseAcc,
                        new DomainEvents.AdminInviteAccepted.Payload(inv.getId(), user.getId(), inv.getEmail())
                )
        );

        log.info("audit=invite.accept inviteId={} adminUserId={}", inv.getId(), user.getId());
        return InviteAcceptResult.ACCEPTED;
    }
}
