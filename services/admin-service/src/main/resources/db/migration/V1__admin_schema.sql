CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.audit_logs(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id uuid,
    action text NOT NULL,
    target_type text NOT NULL,
    target_id uuid,
    payload jsonb,
    ip inet,
    user_agent text,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_created ON app.audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON app.audit_logs(actor_user_id);

CREATE TABLE IF NOT EXISTS app.system_settings(
    key text PRIMARY KEY,
    value jsonb NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'job_type_enum') THEN
        CREATE TYPE app.job_type_enum AS ENUM ('recalculate_plan','send_notifications','cleanup');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'job_status_enum') THEN
        CREATE TYPE app.job_status_enum AS ENUM ('queued','running','succeeded','failed','canceled');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'severity_enum') THEN
        CREATE TYPE app.severity_enum AS ENUM ('info','warning','error');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.background_jobs(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    type app.job_type_enum NOT NULL,
    status app.job_status_enum NOT NULL DEFAULT 'queued',
    run_at timestamptz NOT NULL DEFAULT now(),
    last_run_at timestamptz,
    next_run_at timestamptz,
    attempts integer NOT NULL DEFAULT 0,
    error text
);
CREATE INDEX IF NOT EXISTS idx_jobs_next_run ON app.background_jobs(status, next_run_at);

CREATE TABLE IF NOT EXISTS app.error_logs(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    source text NOT NULL,
    severity app.severity_enum NOT NULL DEFAULT 'error',
    message text NOT NULL,
    context jsonb,
    occurred_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_error_time ON app.error_logs(occurred_at DESC);
