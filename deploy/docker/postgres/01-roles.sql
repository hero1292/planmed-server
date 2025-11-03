DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'auth') THEN
CREATE ROLE auth LOGIN PASSWORD 'auth';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'profile') THEN
CREATE ROLE profile LOGIN PASSWORD 'profile';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'clinical') THEN
CREATE ROLE clinical LOGIN PASSWORD 'clinical';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'planning') THEN
CREATE ROLE planning LOGIN PASSWORD 'planning';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'scheduleq') THEN
CREATE ROLE scheduleq LOGIN PASSWORD 'scheduleq';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'adherence') THEN
CREATE ROLE adherence LOGIN PASSWORD 'adherence';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'export') THEN
CREATE ROLE export LOGIN PASSWORD 'export';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'reporting') THEN
CREATE ROLE reporting LOGIN PASSWORD 'reporting';
END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'adminuser') THEN
CREATE ROLE adminuser LOGIN PASSWORD 'admin';
END IF;
END$$;
