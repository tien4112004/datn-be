#!/usr/bin/env sh
set -e

# gcloud authentication & config
gcloud auth activate-service-account \
  --key-file="${GOOGLE_APPLICATION_CREDENTIALS}" \
  --project="${VERTEX_PROJECT_ID}" \
  --quiet

gcloud config set project "${VERTEX_PROJECT_ID}" --quiet
gcloud config set compute/region "${VERTEX_LOCATION}" --quiet

# Exec the final command (from CMD)
exec "$@"
