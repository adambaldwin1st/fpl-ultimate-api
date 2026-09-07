# FPL Ultimate API

Spring Boot REST API that scrapes and proxies data from the official Fantasy Premier League draft site (`draft.premierleague.com`). The long-term goal is a fully standalone FPL platform with custom rules and scoring, but the current focus is a read-only view of the official FPL data.

## Tech Stack

- **Java 21**, Spring Boot, Maven
- **Spring Data JPA** + **Hibernate** — PostgreSQL (stage/prod), H2 (test)
- **Spring Security** — OAuth2/JWT scaffolded for future auth, not active short-term
- **Lombok** for boilerplate reduction
- **OkHttp3** (`FootballApiClient`) for outbound HTTP calls to the FPL API

## Environments

| Environment | API Client | Database |
|---|---|---|
| local | `FootballApiMockClient` | H2 / local PostgreSQL |
| stage | `FootballApiClientImpl` | PostgreSQL |
| prod | `FootballApiClientImpl` | PostgreSQL |

The mock client (`FootballApiMockClient`) is used locally to avoid hitting the real FPL API during development. Spring profiles control which implementation is active.

## Project Structure

```
src/main/java/com/fpl/ultimate/
├── auth/              # OAuth2/JWT config — scaffolded, not active short-term
├── constants/         # Enums (e.g. FieldPosition)
├── controllers/       # REST controllers
├── models/            # JPA entities
├── repositories/      # Spring Data repositories
├── rest/http/         # FPL API client (interface + real/mock impls)
└── FplUtils.java
```

## Running Locally

```bash
mvn spring-boot:run
```

Requires a `.env` file or environment variables for DB connection and any secrets. Use the mock profile for local dev to avoid hitting the real FPL API.

## Priorities

**Short-term:** Read-only scraping and proxying of official FPL data. Get the API and frontend stood up. Database is a lower priority short-term — stand it up to tee up future work, but don't let it block the read-only goal.

**Long-term:** Fully standalone FPL platform with custom scoring, rules, and write operations.

## Conventions

- Standard Spring Boot conventions throughout
- Lombok is in use — continue using it for new models/services
- Auth (`auth/`) is scaffolded for later — do not wire it into new endpoints short-term without discussion
- Keep `FootballApiClient` as the interface boundary — new FPL API calls go through it, not scattered across controllers
