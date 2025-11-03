package ru.alexanyan.planmed.background.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final JdbcTemplate jdbc;

    @Scheduled(cron = "${jobs.cleanup.cron}")
    public void cleanupExpiredEmailTokens() {
        int emailTokensDeleted = jdbc.update(
                "DELETE FROM auth.email_verification_tokens WHERE expires_at < now() - interval '7 days'"
        );
        int adminInvitesDeleted = jdbc.update(
                "DELETE FROM auth.admin_invites WHERE expires_at < now() - interval '7 days'"
        );
        log.info("cleanup expired tokens: emailTokensDeleted={}, adminInvitesDeleted={}",
                emailTokensDeleted, adminInvitesDeleted);
    }
}
