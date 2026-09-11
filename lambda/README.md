# FPL Draft Lambda Scraper (short-term)

Standalone Python Lambda that scrapes `draft.premierleague.com` and serves
`/standings` and `/current-matchups`, mirroring the logic in
`DraftLeagueService` under `src/main/java/com/fpl/ultimate/draft/`.

This is a short-term stand-in for the Spring Boot API's draft endpoints and is
intentionally decoupled from the Java/Maven build — nothing here is picked up
by `mvn`, and nothing in `src/` depends on this folder.

## Environment variables

- `FPL_DRAFT_LEAGUE_ID` — required, your draft league's ID.
- `FPL_DRAFT_API_HOST` — optional, defaults to `https://draft.premierleague.com`.

## Local test

```bash
cd lambda
FPL_DRAFT_LEAGUE_ID=12345 python3 -c "
from app import get_standings, get_current_matchups
print(get_standings())
print(get_current_matchups())
"
```

## Packaging for deploy

```bash
cd lambda
pip install -r requirements.txt -t package/
cp app.py package/
cd package && zip -r ../function.zip . && cd ..
```

## Deploy (first time)

1. Create the function in the AWS console (or CLI), runtime Python 3.12+,
   handler `app.handler`, upload `function.zip`.
2. Set the two environment variables above.
3. Enable a **Function URL** (Configuration → Function URL → Create), auth
   type `NONE` for a public read-only API.
4. Test: `curl https://<function-url>/standings`

## Redeploy

```bash
aws lambda update-function-code \
  --function-name fpl-ultimate-draft-scraper \
  --zip-file fileb://function.zip
```
