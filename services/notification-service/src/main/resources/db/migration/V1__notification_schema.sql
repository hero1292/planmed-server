CREATE SCHEMA IF NOT EXISTS app;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notification_channel_enum') THEN
        CREATE TYPE app.notification_channel_enum AS ENUM ('email','sms','push');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notification_status_enum') THEN
        CREATE TYPE app.notification_status_enum AS ENUM ('scheduled','sent','failed','canceled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'device_platform_enum') THEN
        CREATE TYPE app.device_platform_enum AS ENUM ('ios','android','web');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.notification_preferences(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL UNIQUE,
    channel_email boolean NOT NULL DEFAULT true,
    channel_sms boolean NOT NULL DEFAULT false,
    channel_push boolean NOT NULL DEFAULT true,
    lead_time_min integer NOT NULL DEFAULT 15,
    timezone text NOT NULL DEFAULT 'UTC',
    quiet_hours jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app.device_tokens(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    platform app.device_platform_enum NOT NULL,
    token text NOT NULL UNIQUE,
    last_seen_at timestamptz
);

CREATE TABLE IF NOT EXISTS app.notification_providers(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    type app.notification_channel_enum NOT NULL,
    config_encrypted bytea NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app.notifications(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    plan_event_id uuid,
    channel app.notification_channel_enum NOT NULL,
    content jsonb NOT NULL,
    scheduled_at timestamptz NOT NULL,
    sent_at timestamptz,
    status app.notification_status_enum NOT NULL DEFAULT 'scheduled',
    provider_id uuid,
    provider_message_id text,
    error_code text,
    retry_count integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notifications_sched_status ON app.notifications(status, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notifications_patient ON app.notifications(patient_id);

CREATE TABLE IF NOT EXISTS app.delivery_attempts(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id uuid NOT NULL REFERENCES app.notifications(id) ON DELETE CASCADE,
    attempt_no smallint NOT NULL,
    requested_at timestamptz NOT NULL DEFAULT now(),
    responded_at timestamptz,
    status app.notification_status_enum NOT NULL,
    provider_response jsonb
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_delivery_attempt_no ON app.delivery_attempts(notification_id, attempt_no);
