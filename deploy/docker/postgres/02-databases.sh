#!/usr/bin/env bash
set -euo pipefail

# Доступны переменные: $POSTGRES_USER, $POSTGRES_DB
# Создаём единую базу данных и схемы под каждый сервис.

DB_NAME="planmed"

# Создаём БД, если нет
if ! psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" | grep -q 1; then
  echo "Creating database ${DB_NAME} (owner ${POSTGRES_USER})"
  createdb -U "$POSTGRES_USER" "${DB_NAME}"
else
  echo "Database ${DB_NAME} already exists, skipping"
fi

# Функция создания схемы с владельцем
create_schema() {
  local schema="$1"
  local owner="$2"
  echo "Ensuring schema '${schema}' with owner '${owner}'"
  psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "${DB_NAME}" -c "CREATE SCHEMA IF NOT EXISTS ${schema} AUTHORIZATION ${owner};"
}

# Создаём схемы для всех сервисов
create_schema auth           auth
create_schema profile        profile
create_schema clinical       clinical
create_schema planning       planning
create_schema scheduleq      scheduleq
create_schema adherence      adherence
create_schema export         export
create_schema reporting      reporting
create_schema admin          adminuser

psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres -c \
"GRANT CONNECT, CREATE ON DATABASE ${DB_NAME} TO auth, profile, clinical, planning, scheduleq, adherence, export, reporting, adminuser;"


echo "✅ All schemas created in database '${DB_NAME}'"
