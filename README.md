<div align="center">

# 📚 BookBuddy

**Discover, track, and share books.**

A full-stack book discovery, tracking, and social reading platform — built as a set of
Spring Boot microservices behind an API gateway, backed by MongoDB, with a React frontend
and JWT authentication. Book data is ingested by scraping the public Open Library API.

<br />

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway-6DB33F?logo=spring&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?logo=mongodb&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-ECS%20Fargate-FF9900?logo=amazonaws&logoColor=white)

<br />

<img width="900" alt="BookBuddy screenshot" src="https://github.com/user-attachments/assets/99cc4de0-0d6e-483b-a6c9-df9052a7f312" />

</div>

<br />

> Built after years of tracking my reading list in a Google Doc and wanting a better
> interface — BookBuddy turns that into a real, social, self-hostable reading platform.

<br />

## 📖 Table of contents

<details open>
<summary>Click to expand</summary>

- [Features](#-features)
- [Tech stack](#-tech-stack)
- [Architecture](#-architecture)
- [API overview](#-api-overview)
- [Run locally with Docker Compose](#-run-locally-with-docker-compose)
- [Run without Docker](#-run-without-docker)
- [Ingest real books from Open Library](#-ingest-real-books-from-open-library)
- [Configuration](#-configuration)
- [Tests](#-tests)
- [Cloud deployment](#-cloud-deployment)
- [Documentation](#-documentation)
- [Contributors](#-contributors)

</details>

<br />

## ✨ Features

- **Discover** — search books by title and browse the top books by rating or wishlist count.
- **Track** — wishlist books, mark them in-progress or finished, and keep a reading streak.
- **Recommendations** — personalized book suggestions and genre-based book club recommendations.
- **Community** — find books you have in common with other readers, and join capacity-limited book clubs.
- **Secure auth** — registration/login with BCrypt-hashed passwords and JWT bearer tokens.

<br />

## 🛠 Tech stack

| Layer        | Technology                                                        |
|--------------|-------------------------------------------------------------------|
| Backend      | Java 21, Spring Boot 3.3, Spring Security, Spring Cloud Gateway    |
| Data         | MongoDB 7 (documents + aggregation)                               |
| Auth         | JWT (JJWT), BCrypt                                                 |
| Frontend     | React 18, React Router 6, Vite 5                                  |
| Ingestion    | Open Library API via Spring `RestClient`                          |
| Local run    | Docker Compose                                                    |
| Cloud        | AWS ECS Fargate (Terraform, see [`deploy/aws`](deploy/aws/README.md)) |

<br />

## 🏗 Architecture

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

| Service             | Port | Responsibility                                                                |
|---------------------|------|-------------------------------------------------------------------------------|
| `api-gateway`       | 8080 | Single entry point for the frontend. Routes to services and validates JWTs.   |
| `catalog-service`   | 8081 | Book search, ratings, top-rated / most-wishlisted rankings, wishlist actions. |
| `discovery-service` | 8082 | Scrapes and normalizes book data from the Open Library API into MongoDB.      |
| `social-service`    | 8083 | Auth (JWT), users, book clubs, recommendations, reading progress, streaks.    |

More detail in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and the data model in
[`docs/DATA_MODEL.md`](docs/DATA_MODEL.md).

<br />

## 🔌 API overview

All requests go through the gateway at `http://localhost:8080`. Except for the two public
auth routes, every request must include a `Authorization: Bearer <token>` header.

| Method | Endpoint                                        | Service    | Description                          |
|--------|-------------------------------------------------|------------|--------------------------------------|
| `POST` | `/api/auth/register`                            | social     | Create an account _(public)_         |
| `POST` | `/api/auth/login`                               | social     | Log in, receive a JWT _(public)_     |
| `GET`  | `/api/auth/me`                                  | social     | Current user profile                 |
| `GET`  | `/api/books/search`                             | catalog    | Search books by title                |
| `GET`  | `/api/books/top`                                | catalog    | Top books by rating / wishlist count |
| `GET`  | `/api/books/{id}`                               | catalog    | Book detail                          |
| `PUT`  | `/api/ratings/{bookId}`                         | catalog    | Rate a book                          |
| `POST` | `/api/wishlist/{bookId}`                        | catalog    | Add a book to the wishlist           |
| `DELETE` | `/api/wishlist/{bookId}`                      | catalog    | Remove a book from the wishlist      |
| `GET`  | `/api/profile/summary`                          | social     | Reading journey summary + streak     |
| `GET`  | `/api/profile/books`                            | social     | Books on the user's reading journey  |
| `POST` | `/api/profile/progress`                         | social     | Update reading progress / status     |
| `GET`  | `/api/profile/books/{bookId}/completion-rate`   | social     | Completion rate for a book           |
| `GET`  | `/api/recommendations/books`                    | social     | Personalized book recommendations    |
| `GET`  | `/api/recommendations/common`                   | social     | Books in common with other readers   |
| `GET`  | `/api/clubs`                                     | social     | List book clubs                      |
| `POST` | `/api/clubs/{clubId}/join`                       | social     | Join a club (capacity-limited)       |
| `GET`  | `/api/clubs/recommendations`                    | social     | Genre-based club recommendations     |
| `POST` | `/api/discovery/ingest`                          | discovery  | Ingest books from Open Library       |

<br />

## 🐳 Run locally with Docker Compose

Requires Docker with the Compose plugin.

```sh
docker compose up --build
```

This starts MongoDB, seeds the sample data, builds and runs all four services and
the frontend. Once healthy:

- Frontend: <http://localhost:3000>
- API gateway: <http://localhost:8080>

Sign in with the demo account **`alex` / `password123`** (other seeded users: `sam`, `jordan`).

<br />

## 💻 Run without Docker

Requires **JDK 21**, **Maven**, **Node 18+**, and a local MongoDB on `mongodb://localhost:27017`.

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

<br />

## 🌐 Ingest real books from Open Library

The discovery service scrapes and stores books on demand:

```sh
curl -X POST http://localhost:8082/api/discovery/ingest \
  -H 'Content-Type: application/json' \
  -d '{"query":"harry potter"}'
```

<br />

## ⚙️ Configuration

Copy `.env.example` to `.env` and adjust as needed. Key variables:

| Variable            | Purpose                                          |
|---------------------|--------------------------------------------------|
| `MONGO_URI`         | MongoDB connection string                        |
| `MONGO_DATABASE`    | MongoDB database name                            |
| `JWT_SECRET`        | Base64 HS256 secret (must match across services) |
| `JWT_EXPIRATION_MS` | Token lifetime                                   |
| `OPEN_LIBRARY_BASE_URL` | Open Library API base URL                    |
| `VITE_API_BASE_URL` | Gateway URL the frontend calls                   |

> ⚠️ The default `JWT_SECRET` is for local development only — set a strong secret in
> any real environment (`openssl rand -base64 32`).

<br />

## 🧪 Tests

```sh
mvn test
```

Unit tests cover the Open Library normalization, rating recomputation, reading
streak transitions, recommendation scoring, and the capacity-safe club join.

<br />

## ☁️ Cloud deployment

Terraform describing an ECS Fargate deployment lives in
[`deploy/aws`](deploy/aws/README.md).

<br />

## 📚 Documentation

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — services, data store, and cross-cutting concerns.
- [`docs/DATA_MODEL.md`](docs/DATA_MODEL.md) — MongoDB collections and document shapes.

<br />

## 👥 Contributors

- [@azka-siddiqui](https://github.com/azka-siddiqui)

<br />

<div align="center">
<sub>Built with ☕ Java, 🍃 Spring Boot, and 📚 a love of reading.</sub>
</div>
