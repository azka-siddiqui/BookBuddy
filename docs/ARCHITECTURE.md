# BookBuddy — Architecture

BookBuddy is a book discovery, tracking, and social reading platform built as a
set of Spring Boot microservices behind an API gateway, backed by MongoDB, with a
React single-page frontend.

## Services

| Service            | Port | Responsibility                                                                 |
|--------------------|------|--------------------------------------------------------------------------------|
| `api-gateway`      | 8080 | Single entry point for the frontend. Routes to services and validates JWTs.    |
| `catalog-service`  | 8081 | Book search, ratings, top-rated / most-wishlisted rankings, wishlist actions.  |
| `discovery-service`| 8082 | Scrapes and normalizes book data from the Open Library API into MongoDB.       |
| `social-service`   | 8083 | Auth (JWT), users, book clubs, recommendations, reading progress, streaks.     |

## Data store

A single MongoDB deployment holds the following primary collections:

- `books` — normalized book documents (title, authors, page count, tags/genres, cover).
- `users` — accounts and credentials (hashed), reading persona, streak state.
- `wishlists` / `reading_progress` — per-user reading journey.
- `ratings` — per-user book ratings feeding top-rated aggregations.
- `book_clubs` / `club_memberships` — social layer with capacity limits.

## Cross-cutting

- **Authentication:** JWT issued by `social-service`. The gateway validates the token
  on every non-public request and injects a trusted `X-User-Id` header for downstream
  services (client-supplied `X-User-Id` is stripped to prevent spoofing). `social-service`
  additionally validates the token itself (defense in depth). Passwords are hashed with
  BCrypt via Spring Security.
- **Public routes:** `POST /api/auth/register` and `POST /api/auth/login` require no token.
- **Inter-service calls:** REST over HTTP using Spring's `RestClient`.
- **Deployment:** Docker Compose for local development; AWS (ECS) manifests/IaC for cloud.

## Frontend

React (Vite) SPA implementing the Home (discover/search), Recommendations, Profile
(reading journey), and Community pages, talking to the gateway over REST.
