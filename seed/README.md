# Seed Data

Sample data used to demonstrate BookBuddy features locally. The dataset mirrors the
books and users shown in the product designs (Alex, Book Club 38, Harry Potter /
Hitchhiker's Guide / Bill Bryson titles, etc.).

## Files

| File                     | Collection         |
|--------------------------|--------------------|
| `books.json`             | `books`            |
| `users.json`             | `users`            |
| `wishlists.json`         | `wishlists`        |
| `ratings.json`           | `ratings`          |
| `reading_progress.json`  | `reading_progress` |
| `book_clubs.json`        | `book_clubs`       |
| `club_memberships.json`  | `club_memberships` |

## Demo credentials

Seeded users share the demo password **`password123`**:

| Username | Display name | Persona         |
|----------|--------------|-----------------|
| `alex`   | Alex         | Casual Reader   |
| `sam`    | Sam          | Avid Reader     |
| `jordan` | Jordan       | Weekend Reader  |

## Password handling

`users.json` intentionally stores a **plaintext `password`** field. This is **for local
demo seeding only**. On startup (dev profile), `social-service` reads this file and
hashes each password with the same BCrypt `PasswordEncoder` used by the real auth flow
before inserting the user. Plaintext passwords are never persisted to MongoDB and never
returned by the API.
