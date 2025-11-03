-- Auth service schema: users, sessions, email_verification_tokens, admin_invites + enums & indexes
CREATE SCHEMA IF NOT EXISTS auth;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE EXTENSION IF NOT EXISTS citext;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status_enum') THEN
CREATE TYPE auth.user_status_enum AS ENUM ('PENDING', 'ACTIVE', 'BLOCKED');
END IF;
END $$;

CREATE TABLE IF NOT EXISTS auth.users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
    login citext NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    status auth.user_status_enum NOT NULL DEFAULT 'PENDING',
    role_code TEXT NOT NULL CHECK (role_code IN ('PATIENT', 'DOCTOR', 'ADMIN')),
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS auth.sessions (
                                            id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
    user_id uuid NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ip inet,
    user_agent TEXT,
    refresh_token_hash TEXT UNIQUE
    );

CREATE INDEX IF NOT EXISTS idx_sessions_user_expires ON auth.sessions (user_id, expires_at);

CREATE TABLE IF NOT EXISTS auth.email_verification_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
    user_id uuid NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_email_tokens_user_exp ON auth.email_verification_tokens (user_id, expires_at)
    WHERE
    used_at IS NULL;

-- Admin invite flow
CREATE TABLE IF NOT EXISTS auth.admin_invites (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid (),
    email TEXT NOT NULL UNIQUE,
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_admin_invites_exp ON auth.admin_invites (expires_at)
    WHERE
    used_at IS NULL;