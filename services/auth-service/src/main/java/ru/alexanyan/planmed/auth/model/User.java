package ru.alexanyan.planmed.auth.model;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLCITextType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "auth", name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Type(PostgreSQLCITextType.class)
    @Column(nullable = false, unique = true, columnDefinition = "citext")
    private String login;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "user_status_enum")
    private UserStatus status = UserStatus.PENDING;

    @Column(nullable = false, name = "role_code")
    private String roleCode; // 'PATIENT'|'DOCTOR'|'ADMIN'

    @Column(nullable = false, name = "mfa_enabled")
    private boolean mfaEnabled = false;

    @Column(nullable = false, name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false, name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
