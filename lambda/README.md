# FPL Draft Lambda Scraper (short-term)

Standalone Python Lambda that scrapes `draft.premierleague.com` and serves
`/league/standings`, `/league/current-matchups`, and `/league/gameweek-points`,
mirroring both the logic and the JSON contract (routes, camelCase keys) of
`DraftLeagueService` under `src/main/java/com/fpl/ultimate/draft/` — so swapping
this Lambda for the real Spring Boot API later needs no frontend changes.

### `/league/gameweek-points?team=<teamName>`

Per-player gameweek points for `team` and its real current-gameweek opponent,
for the head-to-head Points screen. Pulls from four `draft.premierleague.com`
endpoints beyond the league details call the other routes use:
`bootstrap-static` (player/team master data), `entry/{id}/event/{gw}` (a team's
picks), `event/{gw}/live` (per-player points + scoring breakdown), and
`event/{gw}/fixtures` (opponent/home-away/kickoff status).

If the current gameweek's lineups aren't locked yet (that picks endpoint
404s until shortly before its first kickoff), this falls back to the last
*locked* gameweek's roster shown against the real upcoming fixtures — every
player naturally comes back with `started: false` and `opponent`/`isHome` set,
no `points`, since none of those games have happened yet.

This is a short-term stand-in for the Spring Boot API's draft endpoints and is
intentionally decoupled from the Java/Maven build — nothing here is picked up
by `mvn`, and nothing in `src/` depends on this folder.

## Environment variables

- `FPL_DRAFT_LEAGUE_ID` — required, your draft league's ID.
- `FPL_DRAFT_API_HOST` — optional, defaults to `https://draft.premierleague.com`.

Set via the Lambda's environment config — see `../terraform/variables.tf`
(`fpl_draft_league_id`, `fpl_draft_league_id_stage`).

## Infrastructure

Two Lambda functions (`fpl-ultimate-draft-scraper` prod,
`fpl-ultimate-draft-scraper-stage` stage), each behind its own API Gateway
HTTP API with a custom domain (`api.fplultimate.com` /
`stage.api.fplultimate.com`), are provisioned via Terraform in `../terraform/`.
See that directory's resources for the full picture — ACM cert, Route53
records, the GitHub OIDC deploy role, etc.

Terraform is applied locally/manually, not from CI. One-time setup:

```bash
cd ../terraform
cp terraform.tfvars.example terraform.tfvars   # fill in your real league ID
terraform init
terraform plan
terraform apply
```

After `apply`, take the `gha_deploy_role_arn` output and set it as a repo
secret in GitHub: **Settings → Secrets and variables → Actions → Secrets
→ New repository secret** → name `AWS_DEPLOY_ROLE_ARN`, value the role ARN.
That's what lets the deploy workflows authenticate via OIDC with no stored
AWS keys.

## Local test

```bash
cd lambda
FPL_DRAFT_LEAGUE_ID=12345 python3 -c "
from app import get_standings, get_current_matchups
print(get_standings())
print(get_current_matchups())
"
```

## Packaging

```bash
./package.sh
```

Builds `function.zip` (installs anything in `requirements.txt` alongside
`app.py`). Used by both the deploy workflows and manual deploys below.

## Deploying

Normally this happens via GitHub Actions, not manually:

- **Stage**: `Actions → Deploy Lambda (Stage) → Run workflow` — prompts for a
  region (default `us-east-2`) and a branch to deploy.
- **Prod**: publish a GitHub Release. `Deploy Lambda (Prod)` deploys that
  release's commit automatically.

Both just run `aws lambda update-function-code` against the Lambda functions
Terraform already created — they don't touch infrastructure.

### Manual deploy (if needed)

```bash
./package.sh
aws lambda update-function-code \
  --function-name fpl-ultimate-draft-scraper \
  --zip-file fileb://function.zip \
  --region us-east-2
```

Swap the function name for `fpl-ultimate-draft-scraper-stage` to target stage.

## Verifying

```bash
curl https://api.fplultimate.com/league/standings
curl https://stage.api.fplultimate.com/league/current-matchups
curl "https://stage.api.fplultimate.com/league/gameweek-points?team=Rosie%20FC"
```
