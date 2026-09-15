<div align="center">
  <br>

  # BookBuddy
  <i> Discover, track and share books!</i> <br>

A full-stack reading platform with personalized recommendations, reading streaks, and book clubs — built as Spring Boot microservices behind an API gateway, with book data scraped from the Open Library API.

![Java][Java]
![Spring Boot][Spring]
![MongoDB][MongoDB]
![React][React.js]
![Docker][Docker]
</div>

## Setup instructions

BookBuddy runs as four Spring Boot services (an API gateway plus catalog, discovery, and social services), a MongoDB instance, and a React frontend. \
Copy `.env.example` to `.env` in the root of the repository and fill in your values before starting anything. \
The important ones are:
```.env
MONGO_URI=mongodb://localhost:27017
MONGO_DATABASE=bookbuddy
JWT_SECRET=...   # base64-encoded 256-bit secret; generate with: openssl rand -base64 32
JWT_EXPIRATION_MS=86400000
```
> The default `JWT_SECRET` is fine for local development, but set a real one anywhere else — every service has to share the same secret for tokens to validate.

## Screenshots (Demo Acct)
**Home page:** \
<img width="1903" height="924" alt="Page1" src="https://github.com/user-attachments/assets/a1efc607-be11-4018-b8da-45b4d241ecd3" />


**Recommendations page:**\
<img width="1903" height="924" alt="Page2" src="https://github.com/user-attachments/assets/f397a153-07b5-4871-b2d8-fe652404598e" />


**Profile page:**\
<img width="1903" height="924" alt="Page3" src="https://github.com/user-attachments/assets/d207d29f-c64c-477c-be72-d3a748315418" />



### Running with Docker

The quickest way to get everything up is Docker Compose, which brings up MongoDB, seeds the sample data, builds all four services, and serves the frontend:

```bash
docker compose up --build
```

Once the containers are healthy the frontend is on <http://localhost:3000> and the API gateway on <http://localhost:8080>. \
Sign in with the demo account **`alex` / `password123`** (the seed also creates `sam` and `jordan`).

### Running locally

To run the stack by hand you will need **JDK 21**, **Maven**, **Node 18+**, and a local MongoDB on `mongodb://localhost:27017`.

First seed the sample data (books, clubs, reading progress, …):
```bash
MONGO_URI=mongodb://localhost:27017 MONGO_DATABASE=bookbuddy SEED_DIR=./seed \
  sh deploy/seed/seed.sh
```

Then build and start the services. The social service seeds the demo users on startup under the default `dev` profile, so start it first:
```bash
mvn -q -DskipTests package
java -jar services/social-service/target/*.jar
java -jar services/catalog-service/target/*.jar
java -jar services/discovery-service/target/*.jar
java -jar services/api-gateway/target/*.jar
```

Finally, the frontend:
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

To pull real books in from Open Library, the discovery service ingests on demand:
```bash
curl -X POST http://localhost:8082/api/discovery/ingest \
  -H 'Content-Type: application/json' \
  -d '{"query":"harry potter"}'
```

---

## Features Implemented

The application supports the following features, split across the catalog, social, and discovery services:

### Discovery & Catalog
- Search for books by title
- View the top books by rating and by wishlist count
- Add and remove books from a wishlist
- Ingest real book data by scraping the Open Library API

### Social & Reading
- **Tag-Based Book Recommendations:** Suggests books from your tag preferences by finding other readers with similar tastes.
- **Genre-Based Book Clubs:** Recommends clubs that line up with your reading history.
- **Book Completion Rate:** Shows how many readers finish a book after starting it — handy for picking engaging reads.
- **Capacity-Safe Club Join:** Guards the last open slot in a club so concurrent joins stay fair.
- **Reading Streak:** Tracks consecutive reading days to keep you motivated.
- **Secure Auth:** Registration and login with BCrypt-hashed passwords and JWT bearer tokens.

---

## Architecture

Every request from the React SPA goes through the API gateway, which validates the JWT and injects a trusted `X-User-Id` header before routing to the right service. All four services share a single MongoDB deployment.

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

More detail lives in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), the data model in [`docs/DATA_MODEL.md`](docs/DATA_MODEL.md), and the AWS ECS Fargate deployment in [`deploy/aws`](deploy/aws/README.md).

---
## Screenshots
**Home page:** \
<img width="1439" height="761" alt="BookBuddy home page" src="https://github.com/user-attachments/assets/99cc4de0-0d6e-483b-a6c9-df9052a7f312" />

# Tech stack

- **Frontend:** React 18 with React Router and Vite
- **Backend:** Java 21, Spring Boot 3.3, Spring Security, Spring Cloud Gateway (four microservices)
- **Database:** MongoDB 7
- **Auth:** JWT (JJWT) with BCrypt-hashed passwords
- **Ingestion:** Open Library API via Spring `RestClient`
- **Local run / deploy:** Docker Compose locally, AWS ECS Fargate (Terraform) for cloud

---

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[Java]: https://img.shields.io/badge/java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Spring]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[MongoDB]: https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white
[Docker]: https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white
