#!/bin/sh
# One-shot MongoDB seeder. Imports the sample JSON collections into the bookbuddy
# database. Safe to re-run: --drop replaces each collection with the seed contents.
#
# Note: the `users` collection is intentionally NOT imported here — social-service
# seeds users on startup so their passwords are BCrypt-hashed (the users.json file
# holds plaintext demo passwords that must never be stored as-is).
set -e

MONGO_URI="${MONGO_URI:-mongodb://mongo:27017}"
DB="${MONGO_DATABASE:-bookbuddy}"
SEED_DIR="${SEED_DIR:-/seed}"

echo "Seeding database '$DB' at $MONGO_URI"

import() {
  collection="$1"
  file="$SEED_DIR/$collection.json"
  if [ -f "$file" ]; then
    echo "  -> importing $collection"
    mongoimport --uri "$MONGO_URI" --db "$DB" --collection "$collection" \
      --file "$file" --jsonArray --drop
  else
    echo "  !! skipping $collection (no $file)"
  fi
}

import books
import wishlists
import ratings
import reading_progress
import book_clubs
import club_memberships

echo "Seeding complete."
