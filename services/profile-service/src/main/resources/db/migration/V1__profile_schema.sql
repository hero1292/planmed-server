CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.patient_profiles(
    user_id uuid PRIMARY KEY,
    full_name_enc bytea,
    birth_date date,
    phone_enc bytea,
    contact_email_enc bytea,
    address_enc bytea,
    emergency_contact_enc bytea,
    notes_enc bytea,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app.doctor_profiles(
    user_id uuid PRIMARY KEY,
    full_name_enc bytea,
    specialty_enc bytea,
    license_number_enc bytea,
    phone_enc bytea,
    contact_email_enc bytea,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'assignment_status_enum') THEN
        CREATE TYPE app.assignment_status_enum AS ENUM ('active','inactive');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS app.patient_doctor_assignments(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    doctor_id uuid NOT NULL,
    status app.assignment_status_enum NOT NULL DEFAULT 'active',
    assigned_at timestamptz NOT NULL DEFAULT now(),
    ended_at timestamptz,
    comment text
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_assignment_active ON app.patient_doctor_assignments(patient_id) WHERE status='active';

CREATE TABLE IF NOT EXISTS app.consents(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    doctor_id uuid,
    scope text NOT NULL,
    granted_at timestamptz NOT NULL DEFAULT now(),
    revoked_at timestamptz
);
