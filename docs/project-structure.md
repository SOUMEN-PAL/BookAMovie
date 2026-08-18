# Project structure

This is the Maven layout we use now and the layering we keep as the project grows. API contracts live in [v1-api-contract-and-domain.md](v1-api-contract-and-domain.md).

## Current modules

One Maven module per feature. Layers inside a feature are **packages**, not extra jars.

```text
BookMyMovie/                          parent POM (versions, reactor)
├── app/                              Spring Boot entrypoint
├── shared/                           cross-cutting types (no feature code)
├── core/                             infrastructure: JPA base types, later Redis/Kafka/config
└── features/
    ├── auth/                         Security starter + auth code
    ├── user/
    ├── movie/
    ├── theatre/                      includes Screen and Seat
    ├── show/
    ├── booking/
    ├── payment/
    └── review/
```

`app` depends on each feature. Each feature depends on `core`. `core` depends on `shared`. Features do **not** depend on each other. `app` is the composition root.

## Dependencies

| Module | Libraries |
|--------|-----------|
| `shared` | Lombok, Validation, Web MVC |
| `core` | `shared` + Data JPA; Redis/Kafka starters only when those packages exist |
| `features/auth` | `core` + Spring Security |
| other features | `core` only |
| `app` | all features + Flyway, Postgres, Actuator, Boot plugin |

Feature-specific extras (JWT, payment SDK) go on **that feature's POM**. Cross-cutting infrastructure (JPA auditing, Redis, Kafka) belongs on **core**, added when we actually use it.

## What belongs in `shared`

HTTP envelope, errors, pagination, and API prefixes used by every feature. **Target shape** (do not create these classes until a feature needs them). `AppApi`, `WebApi`, and `ApiPaths` already exist.

```text
org.devbot.bookmymovie.shared/
├── api/
│   ├── AppApi.java
│   ├── WebApi.java
│   └── ApiPaths.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ...
├── response/
│   ├── ApiResponse.java
│   └── ErrorResponse.java
├── pagination/
│   └── PageResponse.java
└── validation/
    └── ...
```

- **api** — `/api/v1/app` and `/api/v1/web` prefixes.
- **exception** — shared exception types and one global `@RestControllerAdvice` so app and web APIs return the same error JSON.
- **response** / **pagination** — wrappers, not feature DTOs (`MovieSummaryResponse` stays in `movie.api`).
- **validation** — reusable constraints.

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

Do **not** put in `core`: `@RestController`, feature services, `GrantedAuthority`, `SecurityFilterChain`, Flyway migrations, or `application.properties` (those stay in `app` / features).

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
| Generic `ApiResponse`, `ErrorResponse`, `PageResponse`, global exception handler | `shared` (when implemented) |
| `BaseEntity`, `JpaConfig`, later `RedisConfig` / `KafkaConfig` | `core` |
| Datasource / Flyway / Actuator | `app` |

## Rules that must hold

- Do not put Spring Security on `shared` or `core`. Filters and `SecurityFilterChain` live in `features/auth`.
- Do not depend from `movie` (etc.) onto `auth`. The running app gets Security because `app` depends on `auth`.
- Do not expose JPA entities from controllers. Use DTOs in `api`.
- `domain` may use `data` in the same feature. `api` may use `domain` in the same feature. `api` must not talk to another feature's `data`.
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

Dependency chain if split: `api → domain → data → core`. Other features still must not depend on `auth`. Packages stay the same; only the Maven boundary changes.
