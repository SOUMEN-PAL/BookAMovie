# BookMyMovie

BookMyMovie is a movie-ticket booking backend: browse movies and theatres, pick seats for a show, lock and pay, then manage bookings and reviews. Clients are split into a **mobile/user API** (`/api/v1/app`) and a **theatre-admin / admin / super-admin API** (`/api/v1/web`).

This README follows the current Maven POMs and Spring Boot BOM. Architecture and the frozen v1 API live under [`docs/`](docs/). If a doc disagrees with the version tables below, the POMs are the source of truth.

## Stack Snapshot

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,spring,maven,postgres,hibernate,redis,kafka,docker,aws,git,github&perline=6" alt="BookMyMovie tech stack icons" />
  </a>
</p>

<p align="center">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Spring Boot 4.1.0" src="https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img alt="Maven 3.9.16" src="https://img.shields.io/badge/Maven-3.9.16-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-42.7.11-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img alt="Hibernate 7.4.1" src="https://img.shields.io/badge/Hibernate-7.4.1-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
  <img alt="Flyway 12.4.0" src="https://img.shields.io/badge/Flyway-12.4.0-CC0200?style=for-the-badge&logo=flyway&logoColor=white" />
  <img alt="Lombok 1.18.46" src="https://img.shields.io/badge/Lombok-1.18.46-BC214B?style=for-the-badge" />
</p>

The icon row is a quick overview (Redis, Kafka, Docker, and AWS are **planned**). The tables below are the exact current toolchain.

## What we are building

Roles: `USER`, `THEATER_ADMIN`, `ADMIN`, `SUPER_ADMIN`.

Domain: User, Movie, Review, Theatre, Screen, Seat, Show, Booking, BookingSeat, Payment.

- Auth (register, login, refresh, logout)
- Movies, theatres, screens, seats, and shows
- Seat availability and bookings (later: locks, Redis, concurrent booking)
- Payments (later: webhooks, idempotency)
- Reviews, statistics, and admin dashboards

The API contract is frozen in [docs/v1-api-contract-and-domain.md](docs/v1-api-contract-and-domain.md).

### Build order

```text
Phase 1    Auth, movies, theatres, screens, seats, shows
Phase 2    Bookings, BookingSeat, seat availability
Phase 3    Concurrent booking, transactions, locking
Phase 4    Redis, seat locks, expiration
Phase 5    Payments, idempotency, webhooks
Phase 6    Reviews, statistics, admin dashboard
Phase 7    Docker, Kafka, monitoring, load testing, AWS
```

## Architecture

Modular monolith: `app` is the Spring Boot process. Features are one Maven module each. Layers inside a feature are **packages** (`api` / `domain` / `data`), not extra jars. Features may **import each other one way** (no cycles).

```text
Clients
├─ Mobile / USER          /api/v1/app   (@AppApi)
└─ Theatre admin / web    /api/v1/web   (@WebApi)

App shell
└─ app                    Boot plugin, Flyway, Postgres, Actuator

Features
├─ user                   User
├─ auth                   Spring Security (depends on user)
├─ movie
├─ theatre                Screen + Seat
├─ show                   depends on movie + theatre
├─ booking                Booking + BookingSeat; depends on user + show + theatre
├─ payment                depends on booking
└─ review                 depends on user + movie

Foundations
├─ shared                 Web MVC, validation, API prefixes, later envelopes/errors
└─ core                   Data JPA; BaseEntity, Role/Permission; later Redis, Kafka config
```

Full layout and placement rules: [docs/project-structure.md](docs/project-structure.md).

Feature Maven imports (plus `core` on every feature; `auth` also has Spring Security):

| Feature | Imports |
| --- | --- |
| `user`, `movie`, `theatre` | `core` only |
| `auth` | `user` |
| `show` | `movie`, `theatre` |
| `booking` | `user`, `show`, `theatre` |
| `payment` | `booking` |
| `review` | `user`, `movie` |

## Module Map

| Module | Responsibility |
| --- | --- |
| `app` | `@SpringBootApplication`, `application.properties`, Flyway, Actuator, Postgres driver |
| `shared` | Lombok, Validation, Web MVC; `@AppApi` / `@WebApi` / `ApiPaths`; later `ApiResponse`, pagination, global errors |
| `core` | Data JPA; `BaseEntity`, `JpaConfig`, Role/Permission; Redis/Kafka config when those exist |
| `user` | User entity and profile APIs |
| `auth` | Security starter; filters and `SecurityFilterChain` |
| `movie` | Catalog, now-showing, upcoming |
| `theatre` | Theatres, screens, seats |
| `show` | Showtimes (`@ManyToOne` Movie / Theatre / Screen) |
| `booking` | Bookings and BookingSeat |
| `payment` | Payments and webhooks |
| `review` | Movie reviews |

## Current Toolchain And Versions

Managed by `spring-boot-starter-parent` **4.1.0** unless noted. Resolved from this repo:

### Build And Platform

| Area | Version | Notes |
| --- | --- | --- |
| Java | `17` | `java.version` in the parent POM |
| Spring Boot | `4.1.0` | Parent BOM |
| Maven Wrapper | `3.9.16` | `.mvn/wrapper/maven-wrapper.properties` |
| Maven Wrapper script | `3.3.4` | `wrapperVersion` |
| Project version | `0.0.1-SNAPSHOT` | `org.devbot:BookMyMovie` |
| Compiler plugin | `3.15.0` | Inherited from Boot parent |

### Core Stack (on the classpath today)

| Technology | Version | Where |
| --- | --- | --- |
| Spring Web MVC | Boot `4.1.0` | `shared` (`spring-boot-starter-webmvc`) |
| Validation | Boot `4.1.0` | `shared` |
| Lombok | `1.18.46` | `shared` |
| Spring Data JPA | Boot `4.1.0` | `core` |
| Hibernate | `7.4.1.Final` | via Data JPA |
| Spring Security | Boot `4.1.0` | `features/auth` |
| Flyway | `12.4.0` | `app` + `flyway-database-postgresql` |
| PostgreSQL JDBC | `42.7.11` | `app` (`runtime`) |
| Actuator | Boot `4.1.0` | `app` |

### Testing

| Technology | Version | Notes |
| --- | --- | --- |
| JUnit Jupiter | `6.0.3` | Boot-managed |
| Spring Boot test starters | `4.1.0` | Actuator, JPA, Flyway, Security, Validation, Web MVC on `app` |

### Planned (not on the classpath yet)

| Technology | When |
| --- | --- |
| Redis | Phase 4 — seat locks; config on `core` |
| Kafka | Phase 7 — only if we use it; config on `core` |
| Docker / AWS | Phase 7 |

## HTTP prefixes

| Client | Prefix | Annotation |
| --- | --- | --- |
| USER / mobile | `/api/v1/app` | `@AppApi` |
| THEATER_ADMIN / ADMIN / SUPER_ADMIN | `/api/v1/web` | `@WebApi` |

Defined in `org.devbot.bookmymovie.shared.api`. Put resource paths on methods (`@GetMapping("/movies")`), not a second type-level `@RequestMapping`.

## Build And Run

### Prerequisites

- JDK 17
- PostgreSQL on `localhost:5432`

### Database setup (one time)

Create the database (Spring does not create it for you):

```bash
psql -h localhost -U postgres -c "CREATE DATABASE bookmymovie;"
```

Copy local profile files from the committed templates (gitignored — not in the repo):

```bash
cp app/src/main/resources/application-dev.properties.example app/src/main/resources/application-dev.properties
cp app/src/main/resources/application-prod.properties.example app/src/main/resources/application-prod.properties
```

Dev defaults in the example: database `bookmymovie`, user `postgres`, password `root`.

If Hibernate previously failed on a reserved table name, drop partial schema before restarting:

```sql
DROP TABLE IF EXISTS sessions CASCADE;
```

The `User` entity maps to table `users` (PostgreSQL reserves the name `user`).

### Compile

```bash
./mvnw -DskipTests compile
```

### Run

Dev is the default profile (`spring.profiles.default=dev`):

```bash
./mvnw -pl app spring-boot:run
```

Production — activate at runtime with env vars (no secrets in git):

```bash
SPRING_PROFILES_ACTIVE=prod \
  DATABASE_URL=jdbc:postgresql://host:5432/bookmymovie \
  DATABASE_USERNAME=... \
  DATABASE_PASSWORD=... \
  JWT_ACCESS_SECRET=... \
  JWT_REFRESH_SECRET=... \
  java -jar app/target/bookmymovie-app-0.0.1-SNAPSHOT.jar
```

Or set `SPRING_PROFILES_ACTIVE=prod` in the IDE run configuration and supply the env vars there.

## Docs

| Doc | What it is |
| --- | --- |
| [docs/project-structure.md](docs/project-structure.md) | Modules, packages, what belongs in `shared` / `core` |
| [docs/v1-api-contract-and-domain.md](docs/v1-api-contract-and-domain.md) | Frozen v1 APIs, entities, DTOs, phases |
| [docs/v1-authorities.md](docs/v1-authorities.md) | Role → `SHOW_CREATE`-style authorities |
