# Project structure

This is the Maven layout we use now and the layering we keep as the project grows. API contracts live in [v1-api-contract-and-domain.md](v1-api-contract-and-domain.md).

## Current modules

One Maven module per feature from the v1 entity list (Screen/Seat stay with Theatre; BookingSeat stays with Booking). Layers inside a feature are **packages**, not extra jars.

```text
BookMyMovie/                          parent POM (versions, reactor)
├── app/                              Spring Boot entrypoint
├── shared/                           cross-cutting types (no feature code)
├── core/                             infrastructure: JPA base types, later Redis/Kafka/config
└── features/
    ├── user/                         User
    ├── auth/                         Security starter + auth
    ├── movie/                        Movie
    ├── theatre/                      Theatre, Screen, Seat
    ├── show/                         Show
    ├── booking/                      Booking, BookingSeat
    ├── payment/                      Payment
    └── review/                       Review
```

`app` depends on each feature. `core` depends on `shared`. Features **may import each other one way** (a DAG). `app` is the composition root.

Feature Maven imports (plus `core` on every feature; `auth` also has Spring Security):

| Feature | Imports |
|---------|---------|
| `user`, `movie`, `theatre` | `core` only |
| `auth` | `user` |
| `show` | `movie`, `theatre` |
| `booking` | `user`, `show`, `theatre` |
| `payment` | `booking` |
| `review` | `user`, `movie` |

Cycles are forbidden (`movie` must not depend on `show`; `user` must not depend on `booking`).

## Dependencies

| Module | Libraries |
|--------|-----------|
| `shared` | Lombok, Validation, Web MVC |
| `core` | `shared` + Data JPA; Redis/Kafka starters only when those packages exist |
| `features/user`, `movie`, `theatre` | `core` |
| `features/auth` | `core` + `user` + Spring Security |
| `features/show` | `core` + `movie` + `theatre` |
| `features/booking` | `core` + `user` + `show` + `theatre` |
| `features/payment` | `core` + `booking` |
| `features/review` | `core` + `user` + `movie` |
| `app` | all features + Flyway, Postgres, Actuator, Boot plugin |

Feature-specific extras (JWT, payment SDK) go on **that feature's POM**. Cross-cutting infrastructure (JPA auditing, Redis, Kafka) belongs on **core**, added when we actually use it.

## What belongs in `shared`

HTTP envelope, errors, pagination, and API prefixes used by every feature. Target shape:

```text
org.devbot.bookmymovie.shared/
├── api/
│   ├── AppApi.java
│   ├── WebApi.java
│   └── ApiPaths.java
├── exception/
│   ├── ApiError.java                      ✓
│   ├── GlobalExceptionHandler.java        ✓
│   ├── Session*Exception / User*Exception ✓
│   └── ...
├── response/
│   ├── ApiResponse.java                   (later)
│   └── ...
├── pagination/
│   └── PageResponse.java                  (later)
└── validation/
    ├── ValidPassword.java                 ✓
    └── ValidPasswordValidator.java        ✓
```

- **api** — `/api/v1/app` and `/api/v1/web` prefixes.
- **exception** — shared exception types and `GlobalExceptionHandler` (`ApiError` JSON). Security-specific advice (`BadCredentialsException`, etc.) lives in `features/auth` as `AuthExceptionHandler`.
- **response** / **pagination** — wrappers, not feature DTOs (`MovieSummaryResponse` stays in `movie.api`).
- **validation** — reusable constraints (e.g. `@ValidPassword`).

Do **not** put in `shared`: feature controllers, entities, `SecurityFilterChain`, Redis/JPA infrastructure (`core`), or Flyway (`app`).

## What belongs in `core`

`core` is shared **infrastructure**, not feature code. Add packages when they are needed:

```text
org.devbot.bookmymovie.core/
├── persistence/
│   ├── BaseEntity
│   ├── AuditableEntity
│   └── JpaConfig
├── security/
│   ├── Role
│   ├── Permission
│   └── RolePermissions      # no GrantedAuthority
├── cache/
│   └── RedisConfig          # when we introduce Redis
├── messaging/
│   └── KafkaConfig          # only if we actually use Kafka
└── config/                  # other cross-cutting Spring config
```

- **persistence** — mapped superclasses and JPA setup every feature entity can extend. Feature `@Entity` classes still live in `features/<name>/data`.
- **security** — domain `Role` / `Permission` and the role→permission map. Spring Security types (`GrantedAuthority`, `SecurityFilterChain`) stay in `features/auth`.
- **cache** — Redis connection/template config used by more than one feature. Seat-lock *logic* stays in `booking`.
- **messaging** — Kafka producer/consumer factory only if the project uses Kafka. Do not add the starter “just in case”.
- **config** — clocks, object mappers, shared `@Configuration` that is not HTTP-API.

Do **not** put in `core`: `@RestController`, feature services, feature entities (`User`, `Movie`, `Show`, `Booking`, …), `GrantedAuthority`, `SecurityFilterChain`, Flyway migrations, or `application.properties`.

`core` package root: `org.devbot.bookmymovie.core`.

## Packages inside a feature

Same idea as Android / KMP feature modules: one Gradle/Maven module, layers as source sets.

```text
features/<name>/src/main/java/org/devbot/bookmymovie/<name>/
├── api/        controllers, request/response DTOs, HTTP exception handlers,
│               feature @Configuration that is web/security related
├── domain/     @Service, use cases, business rules
└── data/       @Entity, Spring Data repositories
```

## JPA associations

Classic mappings (`@ManyToOne`, `@OneToMany`, join fetch) are allowed:

- **Inside a feature jar** — e.g. `Theatre` → `Screen` → `Seat` in `theatre`.
- **Toward a Maven dependency** — e.g. `Show` → `Movie` / `Theatre` / `Screen`; `Booking` → `User` / `Show` / `Seat`; `Review` → `User` / `Movie`; `Payment` → `Booking`.

Do **not** map toward a module that must not depend on you (`Movie` must not `@OneToMany Show`).

## HTTP prefixes

| Client | Prefix | Annotation |
|--------|--------|------------|
| USER / mobile | `/api/v1/app` | `@AppApi` |
| THEATER_ADMIN / ADMIN / SUPER_ADMIN | `/api/v1/web` | `@WebApi` |

Defined in `org.devbot.bookmymovie.shared.api`. Put resource paths on **methods**, not a second type-level `@RequestMapping`.

```java
@AppApi
public class MovieAppController {
    @GetMapping("/movies")
    public MovieSummaryResponse list() { ... }
}
```

That serves `GET /api/v1/app/movies`. Use `@WebApi` the same way for `/api/v1/web`.

`app`: `BookMyMovieApplication`, `application.properties`, Flyway config. No feature controllers or entities.

## Where Spring beans go

| Kind | Location |
|------|----------|
| `@RestController`, HTTP DTOs, `SecurityFilterChain`, filters | feature `api` (`auth.api` for security) |
| `@Service` | feature `domain` |
| `@Entity`, `JpaRepository` | feature `data` |
| Generic `ApiResponse`, `PageResponse` | `shared` (when implemented) |
| `ApiError`, `GlobalExceptionHandler` | `shared` ✓ |
| Security exception advice (`AuthExceptionHandler`) | `features/auth` ✓ |
| `BaseEntity`, `JpaConfig`, later `RedisConfig` / `KafkaConfig` | `core` |
| Datasource / Flyway / Actuator | `app` |

## Rules that must hold

- Do not put Spring Security on `shared` or `core`. Filters and `SecurityFilterChain` live in `features/auth`.
- Feature Maven dependencies follow the DAG only. No cycles.
- Do not expose JPA entities from controllers. Use DTOs in `api`.
- `domain` may use `data` in the same feature. `api` may use `domain` in the same feature.
- Screen and Seat stay in `theatre`. `BookingSeat` stays in `booking`.

## Future: split a feature into Maven modules

Keep one jar per feature until a feature is actually large (likely `booking` or `payment`) or another feature must compile against its API without pulling JPA.

Then, and only then:

```text
features/booking/
├── api/      bookmymovie-booking-api
├── domain/   bookmymovie-booking-domain
└── data/     bookmymovie-booking-data
```

Dependency chain if split: `api → domain → data → core`. Packages stay the same; only the Maven boundary changes.
