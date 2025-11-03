CREATE SCHEMA IF NOT EXISTS profile;

CREATE TABLE IF NOT EXISTS profile.patient_profiles(
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

CREATE TABLE IF NOT EXISTS profile.doctor_profiles(
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
        CREATE TYPE profile.assignment_status_enum AS ENUM ('active','inactive');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS profile.patient_doctor_assignments(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    doctor_id uuid NOT NULL,
    status profile.assignment_status_enum NOT NULL DEFAULT 'active',
    assigned_at timestamptz NOT NULL DEFAULT now(),
    ended_at timestamptz,
    comment text
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_assignment_active ON profile.patient_doctor_assignments(patient_id) WHERE status='active';

CREATE TABLE IF NOT EXISTS profile.consents(
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id uuid NOT NULL,
    doctor_id uuid,
    scope text NOT NULL,
    granted_at timestamptz NOT NULL DEFAULT now(),
    revoked_at timestamptz
);
