# BookBuddy — Data Model

MongoDB database: `bookbuddy`. All collections use a string `_id` unless noted.
Timestamps are ISO-8601 UTC strings persisted as BSON dates.

## `books`
Normalized book documents, owned by `catalog-service`, populated by `discovery-service`.

| Field           | Type       | Notes                                                        |
|-----------------|------------|--------------------------------------------------------------|
| `_id`           | string     | Internal id (UUID).                                          |
| `openLibraryId` | string     | Source key from Open Library (e.g. `OL82563W`). Unique.      |
| `title`         | string     | Book title.                                                  |
| `authors`       | string[]   | Author display names.                                        |
| `pageCount`     | int        | Number of pages (0 if unknown).                              |
| `tags`          | string[]   | Genres / subjects (e.g. `War`, `Coming of Age`, `Fantasy`).  |
| `coverUrl`      | string     | Cover image URL (nullable).                                  |
| `description`   | string     | Optional synopsis.                                           |
| `ratingAvg`     | double     | Denormalized average rating, maintained on rating writes.    |
| `ratingCount`   | int        | Number of ratings.                                           |
| `wishlistCount` | int        | Denormalized count, powers "most wishlisted" ranking.        |
| `createdAt`     | date       |                                                              |

Indexes: unique on `openLibraryId`; text index on `title`; `ratingAvg` desc; `wishlistCount` desc.

## `users`
Accounts + reading persona. Owned by `social-service`.

| Field            | Type     | Notes                                                    |
|------------------|----------|----------------------------------------------------------|
| `_id`            | string   | UUID.                                                    |
| `username`       | string   | Unique, login handle (e.g. `alex`).                      |
| `displayName`    | string   | e.g. `Alex`.                                             |
| `passwordHash`   | string   | BCrypt hash. Never returned by the API.                  |
| `persona`        | string   | e.g. `Casual Reader`.                                    |
| `roles`          | string[] | e.g. `["USER"]`.                                         |
| `streakCount`    | int      | Current consecutive-day reading streak (R15).            |
| `lastReadDate`   | date     | Last day a reading event was recorded (streak logic).    |
| `createdAt`      | date     |                                                          |

Indexes: unique on `username`.

## `wishlists`
One document per (user, book) wishlist entry. Owned by `catalog-service`.

| Field       | Type   | Notes                          |
|-------------|--------|--------------------------------|
| `_id`       | string | UUID.                          |
| `userId`    | string | -> users._id                   |
| `bookId`    | string | -> books._id                   |
| `createdAt` | date   |                                |

Indexes: unique compound on (`userId`, `bookId`).

## `ratings`
Per-user rating of a book. Owned by `catalog-service`.

| Field       | Type   | Notes                          |
|-------------|--------|--------------------------------|
| `_id`       | string | UUID.                          |
| `userId`    | string | -> users._id                   |
| `bookId`    | string | -> books._id                   |
| `score`     | int    | 1..5                           |
| `createdAt` | date   |                                |

Indexes: unique compound on (`userId`, `bookId`).

## `reading_progress`
Tracks a user's status/progress per book. Owned by `social-service`.
Feeds the Profile page tiles, completion rate (R13), and streaks (R15).

| Field         | Type   | Notes                                                   |
|---------------|--------|---------------------------------------------------------|
| `_id`         | string | UUID.                                                   |
| `userId`      | string | -> users._id                                            |
| `bookId`      | string | -> books._id                                            |
| `status`      | string | `WISHLIST` | `IN_PROGRESS` | `FINISHED`                  |
| `pageReached` | int    | Current page.                                           |
| `totalPages`  | int    | Denormalized from book at add-time.                     |
| `startedAt`   | date   | When status became IN_PROGRESS.                         |
| `finishedAt`  | date   | When status became FINISHED (nullable).                 |
| `updatedAt`   | date   |                                                         |

Indexes: unique compound on (`userId`, `bookId`); `status`.

## `book_clubs`
Social reading groups. Owned by `social-service`.

| Field         | Type     | Notes                                                     |
|---------------|----------|-----------------------------------------------------------|
| `_id`         | string   | UUID.                                                     |
| `name`        | string   | e.g. `Book Club 38`.                                      |
| `genres`      | string[] | Genre focus, used for recommendations (R12).              |
| `capacity`    | int      | Max members (drives concurrent join limit, R14).          |
| `memberCount` | int      | Denormalized current members.                             |
| `version`     | long     | Optimistic-locking version for safe concurrent joins.     |
| `createdAt`   | date     |                                                           |

Indexes: `genres`.

## `club_memberships`
Join records. Owned by `social-service`.

| Field      | Type   | Notes                          |
|------------|--------|--------------------------------|
| `_id`      | string | UUID.                          |
| `clubId`   | string | -> book_clubs._id              |
| `userId`   | string | -> users._id                   |
| `joinedAt` | date   |                                |

Indexes: unique compound on (`clubId`, `userId`).

## Feature → data mapping

| Feature (design / requirement)      | Collections / mechanism                                  |
|-------------------------------------|----------------------------------------------------------|
| R6 search by title                  | `books` text index                                       |
| R7 add to wishlist                  | `wishlists` (+ `books.wishlistCount`)                    |
| R8 common books between two users   | `reading_progress` intersection aggregation              |
| R9 top-rated                        | `books` sorted by `ratingAvg`                            |
| R10 most wishlisted                 | `books` sorted by `wishlistCount`                        |
| R11 recommendations                 | tag overlap over `reading_progress` + `books.tags`       |
| R12 club recommendations by genre   | `book_clubs.genres` vs user's read `books.tags`          |
| R13 completion rate                 | `reading_progress` FINISHED/started aggregation          |
| R14 concurrent join limit           | `book_clubs.version` optimistic locking + capacity check |
| R15 reading streak                  | `users.streakCount` / `lastReadDate`                     |
