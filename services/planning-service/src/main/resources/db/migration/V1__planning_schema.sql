CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.treatment_plans(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    source_recommendation_id uuid NOT NULL,
    status text NOT NULL DEFAULT 'active',
    generated_at timestamptz NOT NULL DEFAULT now(),
    generation_reason text
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'plan_event_status_enum') THEN
        CREATE TYPE app.plan_event_status_enum AS ENUM ('scheduled','completed','missed','deferred','canceled');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.plan_events(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    treatment_plan_id uuid NOT NULL REFERENCES app.treatment_plans(id) ON DELETE CASCADE,
    recommendation_item_id uuid NOT NULL,
    patient_id uuid NOT NULL,
    scheduled_at timestamptz NOT NULL,
    window_start timestamptz,
    window_end timestamptz,
    timezone text NOT NULL,
    status app.plan_event_status_enum NOT NULL DEFAULT 'scheduled',
    next_notification_at timestamptz,
    cancellation_reason text,
    meta jsonb DEFAULT '{}',
    created_at timestamptz NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'chk_plan_window'
      AND conrelid = 'app.plan_events'::regclass
  ) THEN
ALTER TABLE app.plan_events
    ADD CONSTRAINT chk_plan_window
        CHECK (
            (window_start IS NULL AND window_end IS NULL)
                OR
            (window_start IS NOT NULL AND window_end IS NOT NULL AND window_start <= window_end)
            );
END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_plan_events_patient_date ON app.plan_events(patient_id, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_plan_events_status ON app.plan_events(status);
