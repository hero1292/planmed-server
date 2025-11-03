CREATE SCHEMA IF NOT EXISTS app;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'adherence_status_enum') THEN
        CREATE TYPE app.adherence_status_enum AS ENUM ('taken','skipped','deferred');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'record_source_enum') THEN
        CREATE TYPE app.record_source_enum AS ENUM ('web','mobile','api','system');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.adherence_records(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_event_id uuid NOT NULL UNIQUE,
    patient_id uuid NOT NULL,
    status app.adherence_status_enum NOT NULL,
    actual_taken_at timestamptz,
    comment text,
    recorded_at timestamptz NOT NULL DEFAULT now(),
    record_source app.record_source_enum NOT NULL DEFAULT 'web'
);
CREATE INDEX IF NOT EXISTS idx_adherence_patient_recorded ON app.adherence_records(patient_id, recorded_at DESC);
