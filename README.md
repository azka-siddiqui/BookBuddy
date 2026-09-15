# BookBuddy

Discover, track, and share books. After using Google Docs to track my reading list for years and wanting a better interface, I built BookBuddy: a full-stack book discovery, tracking, and social reading platform built as a set of Spring Boot
microservices behind an API gateway, backed by MongoDB, with a React
frontend and JWT authentication. Book data is ingested by scraping the public
Open Library API.

<img width="1439" height="761" alt="Screenshot 2026-09-14 at 10 07 00 PM" src="https://github.com/user-attachments/assets/99cc4de0-0d6e-483b-a6c9-df9052a7f312" />

## Features

- **Discover** – search books by title and browse the top books by rating or wishlist count
- **Track** – wishlist books, mark them in-progress or finished, and keep a reading streak
- **Recommendations** – personalized book suggestions and genre-based book club recommendations
- **Community** – find books you have in common with other readers, and join capacity-limited book clubs
- **Secure auth** – registration/login with BCrypt-hashed passwords and JWT bearer tokens

## Tech stack

| Layer        | Technology                                              |
|--------------|---------------------------------------------------------|
| Backend      | Java 21, Spring Boot 3.3, Spring Security, Spring Cloud Gateway |
| Data         | MongoDB (documents + aggregation)                       |
| Auth         | JWT (JJWT), BCrypt                                       |
| Frontend     | React 18, React Router, Vite                            |
| Ingestion    | Open Library API via Spring `RestClient`                |
| Local run    | Docker Compose                                          |
| Cloud        | AWS ECS Fargate (Terraform, see `deploy/aws`)           |

## Architecture

```
                     ┌──────────────┐
                     │  React (SPA) │
                     └──────┬───────┘
                            │  REST + JWT
                     ┌──────▼───────┐
                     │  api-gateway │  validates JWT, injects X-User-Id
                     └───┬─────┬────┘
             ┌───────────┘     └───────────┬──────────────┐
     ┌───────▼──────┐  ┌──────────▼─────┐  ┌──────────────▼───┐
     │ catalog-svc  │  │  social-svc    │  │ discovery-svc    │
     │ search,      │  │  auth, clubs,  │  │ Open Library     │
     │ wishlist,    │  │  recs, streaks │  │ scraping         │
     │ ratings      │  │  progress      │  │                  │
     └───────┬──────┘  └────────┬───────┘  └────────┬─────────┘
             └──────────────────┼───────────────────┘
                          ┌─────▼─────┐
                          │  MongoDB  │
                          └───────────┘
```

More detail in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and the data model in
[`docs/DATA_MODEL.md`](docs/DATA_MODEL.md).

## Run locally with Docker Compose

Requires Docker with the Compose plugin.

```sh
docker compose up --build
```

This starts MongoDB, seeds the sample data, builds and runs all four services and
the frontend. Once healthy:

- Frontend: <http://localhost:3000>
- API gateway: <http://localhost:8080>

Sign in with the demo account **`alex` / `password123`** (other seeded users:
`sam`, `jordan`).

## Run without Docker

Requires **JDK 21**, **Maven**, **Node 18+**, and a local MongoDB on
`mongodb://localhost:27017`.

```sh
# 1. Seed sample data (books, clubs, progress, …)
MONGO_URI=mongodb://localhost:27017 MONGO_DATABASE=bookbuddy SEED_DIR=./seed \
  sh deploy/seed/seed.sh

# 2. Build all services
mvn -q -DskipTests package

# 3. Start each service (separate terminals). social-service seeds demo users on
#    startup under the default 'dev' profile.
java -jar services/social-service/target/*.jar
java -jar services/catalog-service/target/*.jar
java -jar services/discovery-service/target/*.jar
java -jar services/api-gateway/target/*.jar

# 4. Frontend
cd frontend && npm install && npm run dev   # http://localhost:5173
```

## Ingest real books from Open Library

The discovery service scrapes and stores books on demand:

```sh
curl -X POST http://localhost:8082/api/discovery/ingest \
  -H 'Content-Type: application/json' \
  -d '{"query":"harry potter"}'
```

## Tests

```sh
mvn test
```

Unit tests cover the Open Library normalization, rating recomputation, reading
streak transitions, recommendation scoring, and the capacity-safe club join.

## Configuration

Copy `.env.example` to `.env` and adjust as needed. Key variables:

| Variable        | Purpose                                          |
|-----------------|--------------------------------------------------|
| `MONGO_URI`     | MongoDB connection string                        |
| `JWT_SECRET`    | Base64 HS256 secret (must match across services) |
| `JWT_EXPIRATION_MS` | Token lifetime                               |

> The default `JWT_SECRET` is for local development only — set a strong secret in
> any real environment (`openssl rand -base64 32`).

## Cloud deployment

Terraform describing an ECS Fargate deployment lives in
[`deploy/aws`](deploy/aws/README.md).
