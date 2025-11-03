CREATE SCHEMA IF NOT EXISTS app;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'export_type_enum') THEN
        CREATE TYPE app.export_type_enum AS ENUM ('schedule','history');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'export_format_enum') THEN
        CREATE TYPE app.export_format_enum AS ENUM ('PDF','CSV');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'job_status_enum') THEN
        CREATE TYPE app.job_status_enum AS ENUM ('queued','running','succeeded','failed','canceled');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.export_jobs(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id uuid NOT NULL,
    type app.export_type_enum NOT NULL,
    format app.export_format_enum NOT NULL,
    date_from date,
    date_to date,
    status app.job_status_enum NOT NULL DEFAULT 'queued',
    file_path text,
    created_at timestamptz NOT NULL DEFAULT now(),
    finished_at timestamptz
);
CREATE INDEX IF NOT EXISTS idx_export_jobs_requester ON app.export_jobs(requester_id, created_at DESC);
