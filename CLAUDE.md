# FPL Ultimate API

Spring Boot REST API that scrapes and proxies data from the official Fantasy Premier League draft site (`draft.premierleague.com`). The long-term goal is a fully standalone FPL platform with custom rules and scoring, but the current focus is a read-only view of the official FPL data.

## Tech Stack

- **Java 21**, Spring Boot, Maven
- **Spring Data JPA** + **Hibernate** — PostgreSQL (stage/prod), H2 (test)
- **Spring Security** — OAuth2/JWT scaffolded for future auth, not active short-term
- **Lombok** for boilerplate reduction
- **OkHttp3** (`FootballApiClient`) for outbound HTTP calls to the football stats API (long-term)
- **WireMock** embedded as a Spring bean (`@Profile("local")`) — stubs the FPL draft API locally

## Environments

| Environment | FPL Draft API | Database |
|---|---|---|
| local | WireMock (embedded, port 8089) | H2 in-memory |
| stage | `draft.premierleague.com` | PostgreSQL |
| prod | `draft.premierleague.com` | PostgreSQL |

WireMock starts automatically with the app on the `local` profile. Stub mappings live in `wiremock/mappings/`.

## Project Structure

```
src/main/java/com/fpl/ultimate/
├── auth/              # OAuth2/JWT config — scaffolded, not active short-term
├── constants/         # Enums (e.g. FieldPosition)
├── controllers/       # Misc controllers (home, health)
├── draft/
│   ├── client/        # FplDraftApiClient — calls draft.premierleague.com
│   ├── controller/    # DraftLeagueController — /league/** endpoints
│   ├── dto/           # Response DTOs (DraftLeagueDetails, Standing, Match, etc.)
│   └── service/       # DraftLeagueService — business logic
├── models/            # JPA entities
├── repositories/      # Spring Data repositories
├── rest/http/         # FootballApiClient — calls football.api-sports.io (long-term stat sourcing)
└── FplUtils.java
```

## Running Locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

WireMock starts automatically on port 8089, serving stubs from `wiremock/mappings/`. No `.env` or external services required for local dev. Bruno collections in `bruno/` can be used to test the API (base URL `http://localhost:5001`).

## Priorities

**Short-term:** Read-only scraping and proxying of official FPL data. Get the API and frontend stood up. Database is a lower priority short-term — stand it up to tee up future work, but don't let it block the read-only goal.

**Long-term:** Fully standalone FPL platform with custom scoring, rules, and write operations.

## Conventions

- Standard Spring Boot conventions throughout
- Lombok is in use — continue using it for new models/services
- Auth (`auth/`) is scaffolded for later — do not wire it into new endpoints short-term without discussion
- `draft/` package owns all FPL draft API interaction — new draft endpoints go there
- `rest/http/` (`FootballApiClient`) is reserved for the long-term football stats API, not draft calls
- Jackson uses `SNAKE_CASE` naming: digits do NOT trigger underscores (`leagueEntry1` → `league_entry1`, not `league_entry_1`)
