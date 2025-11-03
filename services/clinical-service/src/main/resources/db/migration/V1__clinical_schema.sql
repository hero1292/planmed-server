CREATE SCHEMA IF NOT EXISTS app;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'recommendation_status_enum') THEN
        CREATE TYPE app.recommendation_status_enum AS ENUM ('draft','active','closed');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'frequency_type_enum') THEN
        CREATE TYPE app.frequency_type_enum AS ENUM ('times_per_day','interval_hours','cron','prn');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.medications(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    form text,
    strength_value numeric(10,3),
    strength_unit text,
    manufacturer text,
    aliases text[],
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_medication_identity
ON app.medications(lower(name), lower(coalesce(form,'')), strength_value, lower(coalesce(strength_unit,'')));

CREATE TABLE IF NOT EXISTS app.recommendations(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    doctor_id uuid NOT NULL,
    status app.recommendation_status_enum NOT NULL DEFAULT 'draft',
    start_date date NOT NULL,
    end_date date,
    notes_enc bytea,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app.recommendation_items(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_id uuid NOT NULL REFERENCES app.recommendations(id) ON DELETE CASCADE,
    medication_id uuid NOT NULL REFERENCES app.medications(id) ON DELETE RESTRICT,
    dose_value numeric(12,4) NOT NULL,
    dose_unit text NOT NULL,
    frequency_type app.frequency_type_enum NOT NULL,
    times_of_day time[],
    interval_hours integer,
    cron_expr text,
    duration_days integer,
    intake_window_min integer DEFAULT 60,
    with_food text,
    max_daily_dose_value numeric(12,4),
    max_daily_dose_unit text,
    instructions_enc bytea,
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'chk_freq_times'
      AND conrelid = 'app.recommendation_items'::regclass
  ) THEN
ALTER TABLE app.recommendation_items
    ADD CONSTRAINT chk_freq_times CHECK (
        (frequency_type <> 'times_per_day' OR times_of_day IS NOT NULL) AND
        (frequency_type <> 'interval_hours' OR interval_hours IS NOT NULL) AND
        (frequency_type <> 'cron' OR cron_expr IS NOT NULL)
        );
END IF;
END$$;

CREATE TABLE IF NOT EXISTS app.recommendation_versions(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_id uuid NOT NULL REFERENCES app.recommendations(id) ON DELETE CASCADE,
    version_no integer NOT NULL,
    snapshot_json jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE(recommendation_id, version_no)
);
