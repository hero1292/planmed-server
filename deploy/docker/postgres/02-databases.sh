#!/usr/bin/env bash
set -euo pipefail

# Доступны переменные из образа: $POSTGRES_USER, $POSTGRES_DB
# Подключаемся как суперпользователь и проверяем наличие БД.

create_db() {
  local db="$1"
  local owner="$2"
  if ! psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${db}'" | grep -q 1; then
    echo "Creating database ${db} owner ${owner}"
    createdb -U "$POSTGRES_USER" -O "${owner}" "${db}"
  else
    echo "Database ${db} already exists, skipping"
  fi
}

create_db auth_service              auth
create_db profile_service           profile
create_db clinical_service          clinical
create_db planning_service          planning
create_db schedule_query_service    scheduleq
create_db adherence_service         adherence
create_db notification_service      notification
create_db export_service            export
create_db reporting_service         reporting
create_db admin_service             adminuser
