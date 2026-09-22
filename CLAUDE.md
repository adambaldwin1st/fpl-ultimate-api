# FPL Ultimate API

Spring Boot REST API that scrapes and proxies data from the official Fantasy Premier League draft site (`draft.premierleague.com`). The long-term goal is a fully standalone FPL platform with custom rules and scoring, but the current focus is a read-only view of the official FPL data.

**This repo currently contains two separate implementations of the same read-only endpoints — know which one you're touching:**

- **`lambda/`** (Python, standalone AWS Lambda) — this is what's actually live at `api.fplultimate.com` / `stage.api.fplultimate.com` right now, serving the frontend. Short-term stand-in, decoupled from the Maven build.
- **`src/main/java/com/fpl/ultimate/draft/`** (this Spring Boot app) — the intended long-term implementation. Built, tested (WireMock), but **not currently deployed anywhere serving real traffic**. Not wired to `api.fplultimate.com`.

`lambda/app.py`'s docstring describes it as mirroring this Java service's logic and JSON contract, so the two stay swappable later — but until that swap happens, treat `lambda/` as the one users actually hit.

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
lambda/                # LIVE short-term Python Lambda — see its own README.md
├── app.py             # handler(event, context); routes on event["rawPath"]
├── package.sh          # builds function.zip for deploy
└── requirements.txt    # stdlib only currently

terraform/              # IaC for both Lambda functions, API Gateway, ACM, Route53, the OIDC deploy role.
                         # Applied locally/manually, not from CI — see lambda/README.md.

src/main/java/com/fpl/ultimate/   # Long-term Spring Boot implementation, not currently live
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

## Deploying the Lambda

- Push to `main` touching `lambda/**` → auto-deploys stage. Also runnable manually (`Actions → Deploy Lambda (Stage)`, any branch).
- Publish a GitHub Release → deploys that release's commit to prod (`Deploy Lambda (Prod)`).
- Both authenticate via OIDC (`AWS_DEPLOY_ROLE_ARN` repo secret) — no stored AWS credentials.
- Full detail in `lambda/README.md`.

Note: `.github/workflows/deploy-api.yml` / `deploy-dev-api.yml` are unrelated — they deploy this Spring Boot app to a Raspberry Pi, a separate target from the Lambda above.

## Priorities

**Short-term:** Read-only scraping and proxying of official FPL data. Get the API and frontend stood up — **done**, live via `lambda/` (see above). Database is a lower priority short-term — stand it up to tee up future work, but don't let it block the read-only goal.

**Long-term:** Fully standalone FPL platform with custom scoring, rules, and write operations.

## Conventions

- Standard Spring Boot conventions throughout
- Lombok is in use — continue using it for new models/services
- Auth (`auth/`) is scaffolded for later — do not wire it into new endpoints short-term without discussion
- `draft/` package owns all FPL draft API interaction — new draft endpoints go there. Remember it isn't live short-term (see top of this file) — a change here won't show up for real users until it replaces `lambda/`.
- Short-term read-only endpoint changes (matching what the frontend actually calls today) go in `lambda/app.py`, not `draft/` — see `lambda/README.md`.
- `rest/http/` (`FootballApiClient`) is reserved for the long-term football stats API, not draft calls
- Jackson uses `SNAKE_CASE` naming: digits do NOT trigger underscores (`leagueEntry1` → `league_entry1`, not `league_entry_1`)
