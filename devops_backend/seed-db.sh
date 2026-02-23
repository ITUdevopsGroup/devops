#!/bin/sh
set -eu

DB_PATH="${MINITWIT_DB_PATH:-/data/minitwit.db}"
SEED_PATH="/app/seed/minitwit.db"

mkdir -p "$(dirname "$DB_PATH")"

# If no db exists in the volume yet, copy the seed db once
if [ ! -f "$DB_PATH" ]; then
  echo "Seeding database to $DB_PATH"
  cp "$SEED_PATH" "$DB_PATH"
fi

# Start the app
exec java -jar /app/app.jar
