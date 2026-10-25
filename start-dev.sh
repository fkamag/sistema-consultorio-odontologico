#!/usr/bin/env bash
set -a
source "$(dirname "$0")/.env"
set +a

# Mapeia variáveis do .env para os nomes esperados pelo Spring
export SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD}"
export MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9004}"

cd "$(dirname "$0")/backend"
exec mvn spring-boot:run
